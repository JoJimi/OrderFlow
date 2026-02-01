package org.example.payment.dto.toss;

import jakarta.validation.constraints.NotBlank;

/**
 * 결제 취소 요청 DTO
 */
public record TossPaymentCancelRequest(
        @NotBlank(message = "취소 사유는 필수입니다")
        String cancelReason,

        Long cancelAmount  // null이면 전액 취소
) {
    public static TossPaymentCancelRequest fullCancel(String reason) {
        return new TossPaymentCancelRequest(reason, null);
    }

    public static TossPaymentCancelRequest partialCancel(String reason, Long amount) {
        return new TossPaymentCancelRequest(reason, amount);
    }
}