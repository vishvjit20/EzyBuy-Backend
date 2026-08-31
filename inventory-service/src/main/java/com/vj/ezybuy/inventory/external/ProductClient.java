package com.vj.ezybuy.inventory.external;

import com.vj.ezybuy.common.payload.ProductSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "${PRODUCT_SERVICE_NAME}", url = "${PRODUCT_SERVICE_URL:}")
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ProductSnapshot getProductById(@PathVariable UUID productId);


}