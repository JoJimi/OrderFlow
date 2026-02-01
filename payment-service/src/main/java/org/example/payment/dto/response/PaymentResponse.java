package org.example.payment.dto.response;

import org.example.payment.domain.Payment;
import org.example.shared.type.payment.PaymentMethod;
import org.example.shared.type.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record PaymentResponse(
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,

        // Toss 관련 필드
        String tossOrderId,
        String tossPaymentKey,
        String transactionId,
        OffsetDateTime approvedAt,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        String receiptUrl,

        // 실패 정보
        String failureCode,
        String failureReason,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod(),
                payment.getTossOrderId(),
                payment.getTossPaymentKey(),
                payment.getTransactionId(),
                payment.getApprovedAt(),
                payment.getCardCompany(),
                payment.getCardNumber(),
                payment.getInstallmentMonths(),
                payment.getReceiptUrl(),
                payment.getFailureCode(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}