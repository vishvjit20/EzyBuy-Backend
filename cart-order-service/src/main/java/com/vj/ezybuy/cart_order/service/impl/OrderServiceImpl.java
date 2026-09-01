package com.vj.ezybuy.cart_order.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import com.vj.ezybuy.cart_order.client.InventoryClient;
import com.vj.ezybuy.cart_order.client.ProductClient;
import com.vj.ezybuy.cart_order.dto.*;
import com.vj.ezybuy.cart_order.entity.*;
import com.vj.ezybuy.cart_order.exception.BusinessRuleException;
import com.vj.ezybuy.cart_order.exception.ExternalServiceException;
import com.vj.ezybuy.cart_order.exception.ResourceNotFoundException;
import com.vj.ezybuy.cart_order.repository.CartRepository;
import com.vj.ezybuy.cart_order.repository.OrderRepository;
import com.vj.ezybuy.cart_order.service.OrderService;
import com.vj.ezybuy.common.payload.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    private final RestClient restClient;

    private final RestTemplate restTemplate;
    private final ProductClient productClient;

    private ProductSnapshot getProduct(String productId) {

        try {

            ProductSnapshot productById = productClient.getProductById(UUID.fromString(productId));
            if (productById == null) {
                return new ProductSnapshot(
                        UUID.randomUUID(),
                        "Demo Product",
                        "This is demo product",
                        "This is long desc of demo product",
                        23432.234,
                        500,
                        true
                );
            }

            return productById;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw new RuntimeException("Product not found " + e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("something went wrong");
        }

    }

    @Override
    public ProductSnapshot createOrder(OrderCreateRequest orderCreateRequest) {
        log.info("retry trying....");
        String productId = orderCreateRequest.items().getFirst().productId();
        return this.getProduct(productId);
    }

    public ProductSnapshot createOrderFallback(OrderCreateRequest orderCreateRequest, Throwable t) {
        log.info("Create order fallback...");
        log.info("exception {} ", t.getMessage());
        return null;
    }

    @Override
    public OrderResponse checkout(String userId, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserIdAndStatus(normalizeUserId(userId), CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for userId: " + userId));


        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }

        List<InventorySnapshot> reservedSnapshots = new ArrayList<>();

        try {
            for (CartItem item : cart.getItems()) {
                reservedSnapshots.add(
                        inventoryClient.reserveByProductId(
                                item.getProductId(),
                                new ReserveStockRequest(item.getQuantity())));
            }

            Order order = buildOrderFromCart(cart, request);
            Order saved = orderRepository.save(order);

            saved = orderRepository.save(saved);

            cart.setStatus(CartStatus.CHECKED_OUT);
            cart.setCheckedOutAt(Instant.now());
            cart.getItems().clear();
            cartRepository.save(cart);

            return toResponse(saved);
        } catch (RuntimeException ex) {
            for (int i = reservedSnapshots.size() - 1; i >= 0; i--) {
                CartItem item = cart.getItems().get(i);
                try {
                    inventoryClient.releaseByProductId(item.getProductId(), new ReleaseStockRequest(item.getQuantity()));
                } catch (Exception releaseEx) {
                    throw new ExternalServiceException("Checkout failed and stock rollback also failed for productId: " + item.getProductId(), releaseEx);
                }
            }
            if (ex instanceof ExternalServiceException externalServiceException) {
                throw externalServiceException;
            }
            throw new ExternalServiceException("Checkout failed", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return toResponse(orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId)));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        return toResponse(orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for orderNumber: " + orderNumber)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(normalizeUserId(userId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(order);
        }
        for (OrderItem item : order.getItems()) {
            try {
                inventoryClient.releaseByProductId(item.getProductId(), new ReleaseStockRequest(item.getQuantity()));
            } catch (Exception ex) {
                throw new ExternalServiceException("Failed to release stock for productId: " + item.getProductId(), ex);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public void releaseReservedStock(UUID productId, Integer quantity) {
        try {
            inventoryClient.releaseByProductId(productId, new ReleaseStockRequest(quantity));
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to release stock for productId: " + productId, ex);
        }
    }

    private Order buildOrderFromCart(Cart cart, CheckoutRequest request) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUserId(cart.getUserId());
        order.setBillingName(request.billingName().trim());
        order.setBillingPhone(request.billingPhone().trim());
        order.setExtraInformation(request.extraInformation().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = getOrderItem(cartItem, order);
            order.getItems().add(orderItem);
            total = total.add(orderItem.getLineTotal());
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        return order;
    }

    private static @NonNull OrderItem getOrderItem(CartItem cartItem, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductTitle(cartItem.getProductTitle());
        orderItem.setUnitPrice(cartItem.getUnitPrice());
        orderItem.setDiscountPercent(cartItem.getDiscountPercent());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setLineTotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())).setScale(2, RoundingMode.HALF_UP));
        return orderItem;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getBillingName(),
                order.getBillingPhone(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getShippingAddress(),
                order.getPaymentStatus(),
                order.getExtraInformation(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCancelledAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getDiscountPercent(),
                item.getQuantity(),
                item.getLineTotal());
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessRuleException("userId is required");
        }
        return userId.trim();
    }

    @Override
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        log.info("Updating payment status for Order ID: {} to {}", orderId, paymentStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));
        
        PaymentStatus parsedStatus = PaymentStatus.valueOf(paymentStatus);
        order.setPaymentStatus(parsedStatus);

        if (parsedStatus == PaymentStatus.FAILED) {
            log.warn("Payment failed for Order ID: {}. Triggering compensation: cancelling order.", orderId);
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
        }

        orderRepository.save(order);
        log.info("Payment status successfully updated for Order ID: {}", orderId);
    }
}
