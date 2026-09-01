package com.vj.ezybuy.cart_order.dto;

import java.util.List;

public record OrderCreateRequest(
        List<Item> items

) {
}
