package org.example.order.dto.response;

public record InventoryInfoResponse(
        String inventoryId,
        String productId,
        Integer totalStock,
        Integer reservedStock,
        Integer availableStock
) {}