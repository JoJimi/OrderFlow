package org.example.shared.dto;

import org.example.shared.type.inventory.InventoryAction;
import org.example.shared.type.inventory.InventoryEventType;

import java.time.LocalDateTime;

public record InventoryEvent(
        String inventoryId,
        String productId,
        String orderId,
        Integer quantity,
        InventoryAction action,
        InventoryEventType eventType,
        String reason,
        LocalDateTime timestamp
) {
    /**
     * 재고 예약 이벤트 생성
     */
    public static InventoryEvent reserved(
            String inventoryId,
            String productId,
            String orderId,
            Integer quantity
    ) {
        return new InventoryEvent(
                inventoryId,
                productId,
                orderId,
                quantity,
                InventoryAction.RESERVE,
                InventoryEventType.INVENTORY_RESERVED,
                "주문 생성으로 인한 재고 예약",
                LocalDateTime.now()
        );
    }

    public static InventoryEvent insufficient(
            String productId,
            String orderId,
            Integer requestedQuantity,
            Integer availableStock
    ) {
        return new InventoryEvent(
                null,
                productId,
                orderId,
                requestedQuantity,
                null,
                InventoryEventType.INVENTORY_INSUFFICIENT,
                String.format("재고 부족 - 요청: %d, 사용가능: %d", requestedQuantity, availableStock),
                LocalDateTime.now()
        );
    }

    public static InventoryEvent deducted(
            String inventoryId,
            String productId,
            String orderId,
            Integer quantity
    ) {
        return new InventoryEvent(
                inventoryId,
                productId,
                orderId,
                quantity,
                InventoryAction.DEDUCT,
                InventoryEventType.INVENTORY_DEDUCTED,
                "결제 완료로 인한 재고 차감",
                LocalDateTime.now()
        );
    }

    public static InventoryEvent restored(
            String inventoryId,
            String productId,
            String orderId,
            Integer quantity
    ) {
        return new InventoryEvent(
                inventoryId,
                productId,
                orderId,
                quantity,
                InventoryAction.RESTORE,
                InventoryEventType.INVENTORY_RESTORED,
                "결제 실패로 인한 재고 복구 (보상 트랜잭션)",
                LocalDateTime.now()
        );
    }
}