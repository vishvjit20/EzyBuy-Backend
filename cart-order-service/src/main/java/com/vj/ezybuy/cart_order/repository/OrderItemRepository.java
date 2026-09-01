package com.vj.ezybuy.cart_order.repository;

import com.vj.ezybuy.cart_order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
