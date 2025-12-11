package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 배송 상태 Enum
 *
 * 상태 전이:
 * SHIPPING_PREPARING → SHIPPING_STARTED → SHIPPING_COMPLETED
 */
@Getter
@RequiredArgsConstructor
public enum ShippingStatus {
    SHIPPING_PREPARING("배송 준비중", Set.of("SHIPPING_STARTED")),
    SHIPPING_STARTED("배송 시작", Set.of("SHIPPING_COMPLETED")),
    SHIPPING_COMPLETED("배송 완료", Set.of());

    private final String description;
    private final Set<String> allowedTransitions;

    /**
     * 다음 상태로 전환 가능한지 확인
     */
    public boolean canTransitionTo(ShippingStatus nextStatus) {
        return allowedTransitions.contains(nextStatus.name());
    }

    /**
     * 코드로부터 ShippingStatus 찾기
     */
    public static ShippingStatus fromCode(String code) {
        try {
            return ShippingStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 배송 상태 코드: " + code);
        }
    }
}