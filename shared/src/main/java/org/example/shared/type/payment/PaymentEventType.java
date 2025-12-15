package org.example.shared.type.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentEventType {
    PAYMENT_COMPLETED("결제 완료"),
    PAYMENT_FAILED("결제 실패"),
    PAYMENT_CANCELLED("결제 취소");

    private final String description;
}