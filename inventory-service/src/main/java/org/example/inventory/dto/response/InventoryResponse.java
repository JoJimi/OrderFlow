package org.example.inventory.dto.response;

import org.example.inventory.domain.Inventory;

import java.time.LocalDateTime;

/**
 * 재고 응답 DTO
 */
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

    /**
     * 재고 일관성 확인
     */
    public boolean isConsistent() {
        return totalStock.equals(availableStock + reservedStock);
    }
}