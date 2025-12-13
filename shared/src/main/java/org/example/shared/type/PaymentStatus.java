package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PAYMENT_PENDING("결제 대기", Set.of("PAYMENT_COMPLETED", "PAYMENT_FAILED")),
    PAYMENT_COMPLETED("결제 완료", Set.of()),
    PAYMENT_FAILED("결제 실패", Set.of());

    private final String description;
    private final Set<String> allowedTransitions;

    public boolean canTransitionTo(PaymentStatus nextStatus) {
        return allowedTransitions.contains(nextStatus.name());
    }

    public static PaymentStatus fromCode(String code) {
        try {
            return PaymentStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 결제 상태 코드: " + code);
        }
    }
}