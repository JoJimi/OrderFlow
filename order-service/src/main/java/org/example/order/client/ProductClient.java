package org.example.order.client;

import org.example.order.dto.response.ProductInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        url = "http://localhost:8082",
        fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {
    @GetMapping("/api/products/{productId}")
    ProductInfoResponse getProduct(@PathVariable("productId") String productId);
}
