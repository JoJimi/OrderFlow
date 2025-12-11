package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 수단 Enum
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    PAYPAL("페이팔"),
    BANK_TRANSFER("계좌이체"),
    MOBILE_PAYMENT("간편결제");

    private final String description;

    /**
     * 코드로부터 PaymentMethod 찾기
     */
    public static PaymentMethod fromCode(String code) {
        try {
            return PaymentMethod.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 결제 수단 코드: " + code);
        }
    }

    /**
     * 결제 수단이 온라인 결제인지 확인
     */
    public boolean isOnlinePayment() {
        return this == CREDIT_CARD ||
                this == DEBIT_CARD ||
                this == PAYPAL ||
                this == MOBILE_PAYMENT;
    }
}