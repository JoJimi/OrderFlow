package org.example.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Toss Payments API 에러 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResponse(
        String code,
        String message
) {
    /**
     * 재시도 가능한 에러인지 확인
     */
    public boolean isRetryable() {
        return code != null && (
                code.startsWith("PROVIDER_") ||
                        "FAILED_INTERNAL_SYSTEM_PROCESSING".equals(code) ||
                        "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING".equals(code)
        );
    }

    /**
     * 잔액 부족 에러인지 확인
     */
    public boolean isInsufficientBalance() {
        return "REJECT_CARD_PAYMENT".equals(code) ||
                "INSUFFICIENT_BALANCE".equals(code);
    }
}