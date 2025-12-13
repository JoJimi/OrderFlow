package org.example.payment.dto.response;

import org.example.payment.domain.Payment;
import org.example.shared.type.PaymentMethod;
import org.example.shared.type.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String transactionId,
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
                payment.getTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}