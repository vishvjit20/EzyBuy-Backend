package com.vj.ezybuy.cart_order.client.fallbacks;

import com.vj.ezybuy.cart_order.client.ProductClient;
import com.vj.ezybuy.common.payload.ProductSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {


    @Override
    public ProductSnapshot getProductById(UUID productId) {
        log.info("product fallback ");
        return null;
    }
}