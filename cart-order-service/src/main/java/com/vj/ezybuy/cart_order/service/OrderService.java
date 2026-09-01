package com.vj.ezybuy.cart_order.service;

import com.vj.ezybuy.cart_order.dto.CheckoutRequest;
import com.vj.ezybuy.cart_order.dto.OrderCreateRequest;
import com.vj.ezybuy.cart_order.dto.OrderResponse;
import com.vj.ezybuy.common.payload.ProductSnapshot;

import java.util.List;
import java.util.UUID;


public interface OrderService {


	//learning :
	ProductSnapshot createOrder(OrderCreateRequest orderCreateRequest) ;

	//for project
	OrderResponse checkout(String userId, CheckoutRequest request);

	OrderResponse getOrderById(Long orderId);

	OrderResponse getOrderByNumber(String orderNumber);

	List<OrderResponse> getOrdersByUserId(String userId);

	OrderResponse cancelOrder(Long orderId);

	void releaseReservedStock(UUID productId, Integer quantity);

	void updatePaymentStatus(Long orderId, String paymentStatus);
}
