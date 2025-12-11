package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 재고 액션 타입 Enum
 */
@Getter
@RequiredArgsConstructor
public enum InventoryAction {
    RESERVE("재고 예약", "주문 생성 시 재고를 예약합니다"),
    DEDUCT("재고 차감", "결제 완료 시 예약된 재고를 실제로 차감합니다"),
    RESTORE("재고 복구", "결제 실패 시 예약된 재고를 복구합니다 (보상 트랜잭션)"),
    INCREASE("재고 증가", "입고 시 재고를 증가시킵니다");

    private final String description;
    private final String detail;

    /**
     * 코드로부터 InventoryAction 찾기
     */
    public static InventoryAction fromCode(String code) {
        try {
            return InventoryAction.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 재고 액션 코드: " + code);
        }
    }

    /**
     * 보상 트랜잭션 액션인지 확인
     */
    public boolean isCompensation() {
        return this == RESTORE;
    }

    /**
     * 재고 감소 액션인지 확인
     */
    public boolean isDecreasing() {
        return this == RESERVE || this == DEDUCT;
    }

    /**
     * 재고 증가 액션인지 확인
     */
    public boolean isIncreasing() {
        return this == RESTORE || this == INCREASE;
    }
}