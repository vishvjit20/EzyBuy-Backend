package com.vj.ezybuy.cart_order.service;


import com.vj.ezybuy.cart_order.dto.AddCartItemRequest;
import com.vj.ezybuy.cart_order.dto.CartResponse;
import com.vj.ezybuy.cart_order.dto.UpdateCartItemRequest;

public interface CartService {

	//get card by userid
	CartResponse getCart(String userId);

	// add item to cart
	CartResponse addItem(String userId, AddCartItemRequest request);

	//updating the quantity
	CartResponse updateItem(String userId, String productId, UpdateCartItemRequest request);

	//remove the item from cart
	CartResponse removeItem(String userId, String productId);

	//cart clear
	void clearCart(String userId);
}
