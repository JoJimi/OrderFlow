package org.example.order.client;

import org.example.order.dto.response.InventoryInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "inventory-service",
        fallbackFactory = InventoryClientFallbackFactory.class
)
public interface InventoryClient {

    @GetMapping("/api/inventory/{productId}")
    InventoryInfoResponse getInventory(@PathVariable("productId") String productId);
}
