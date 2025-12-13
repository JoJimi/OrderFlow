package org.example.inventory.dto.response;

import org.example.inventory.domain.Inventory;

import java.time.LocalDateTime;

public record InventoryResponse(
        String inventoryId,
        String productId,
        Integer totalStock,
        Integer reservedStock,
        Integer availableStock,
        LocalDateTime lastUpdated
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getTotalStock(),
                inventory.getReservedStock(),
                inventory.getAvailableStock(),
                inventory.getUpdatedAt()
        );
    }

    public boolean isConsistent() {
        return totalStock.equals(availableStock + reservedStock);
    }
}