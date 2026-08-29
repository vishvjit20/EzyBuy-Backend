package com.vj.ezybuy.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    private final String productServiceId;

    public RouteConfig(@Value("${product.service.id}") String productServiceId) {
        this.productServiceId = productServiceId;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "product-route",
                        route -> route
                                .path("/products/**")
                                .filters(f -> f.rewritePath("/products/?(?<remaining>.*)", "/${remaining}"))
                                .uri(productServiceId))
                .build();
    }
}
