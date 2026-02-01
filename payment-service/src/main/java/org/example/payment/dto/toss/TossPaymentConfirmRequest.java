package org.example.payment.dto.toss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 프론트엔드에서 결제 승인 요청 시 전달하는 DTO
 * Toss 결제 위젯에서 결제 완료 후 반환받는 값들
 */
public record TossPaymentConfirmRequest(
        @NotBlank(message = "paymentKey는 필수입니다")
        String paymentKey,

        @NotBlank(message = "orderId는 필수입니다")
        String orderId,

        @NotNull(message = "amount는 필수입니다")
        @Positive(message = "amount는 양수여야 합니다")
        Long amount
) {}