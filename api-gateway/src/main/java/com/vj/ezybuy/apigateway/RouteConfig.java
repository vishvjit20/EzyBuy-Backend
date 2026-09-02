package com.vj.ezybuy.apigateway;

import com.vj.ezybuy.apigateway.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class RouteConfig {

    @Value("${product.service.id}")
    private final String productServiceId;
    private final AuthenticationFilter authenticationFilter;

    public RouteConfig(@Value("${product.service.id}") String productServiceId,
                       AuthenticationFilter authenticationFilter) {
        this.productServiceId = productServiceId;
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "product-route",
                        route -> route
                                .path("/products/**")
                                .filters(f ->
//                                        f.retry(retryConfig -> retryConfig
//                                                        .setRetries(3)
//                                                        .setMethods(HttpMethod.GET, HttpMethod.POST)
//                                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
//                                        f.requestRateLimiter(rateLimitingConfig -> rateLimitingConfig.setKeyResolver(keyResolver()).setRateLimiter(redisRateLimiter()))
                                        f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                         .circuitBreaker(c -> c.setName("productCircuitBreaker").setFallbackUri("forward:/product-fallback"))
                                         .rewritePath("/products/?(?<remaining>.*)", "/${remaining}"))
                                .uri(productServiceId))
                .build();
    }


    @Bean
    public KeyResolver keyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("user"));
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(4, 4, 1);
    }
}
