package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum ShippingStatus {
    SHIPPING_PREPARING("배송 준비중", Set.of("SHIPPING_STARTED")),
    SHIPPING_STARTED("배송 시작", Set.of("SHIPPING_COMPLETED")),
    SHIPPING_COMPLETED("배송 완료", Set.of());

    private final String description;
    private final Set<String> allowedTransitions;

    public boolean canTransitionTo(ShippingStatus nextStatus) {
        return allowedTransitions.contains(nextStatus.name());
    }

    public static ShippingStatus fromCode(String code) {
        try {
            return ShippingStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 배송 상태 코드: " + code);
        }
    }
}