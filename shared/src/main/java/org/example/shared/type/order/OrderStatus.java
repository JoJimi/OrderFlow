package org.example.shared.type.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDER_CREATED("주문 생성", Set.of("PAYMENT_PENDING", "CANCELLED")),
    PAYMENT_PENDING("결제 대기", Set.of("PAYMENT_COMPLETED", "PAYMENT_FAILED", "CANCELLED")),
    PAYMENT_COMPLETED("결제 완료", Set.of("SHIPPING_PREPARED")),
    PAYMENT_FAILED("결제 실패", Set.of("CANCELLED")),
    SHIPPING_PREPARED("배송 준비", Set.of("SHIPPING_STARTED")),
    SHIPPING_STARTED("배송 시작", Set.of("SHIPPING_COMPLETED")),
    SHIPPING_COMPLETED("배송 완료", Set.of()),
    CANCELLED("주문 취소", Set.of());

    private final String description;
    private final Set<String> allowedTransitions;

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return allowedTransitions.contains(nextStatus.name());
    }

    public static OrderStatus fromCode(String code) {
        try {
            return OrderStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 주문 상태 코드: " + code);
        }
    }
}
