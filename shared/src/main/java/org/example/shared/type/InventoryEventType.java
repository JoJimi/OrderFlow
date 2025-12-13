package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryEventType {
    INVENTORY_RESERVED("재고 예약", "주문 생성 시 재고 예약 완료"),
    INVENTORY_INSUFFICIENT("재고 부족", "주문 생성 시 재고 부족으로 예약 실패"),
    INVENTORY_DEDUCTED("재고 차감", "결제 완료 후 재고 차감 완료"),
    INVENTORY_RESTORED("재고 복구", "결제 실패 후 예약 재고 복구 완료");

    private final String description;
    private final String detail;
}