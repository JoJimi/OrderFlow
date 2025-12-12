package org.example.shared.dto;

import org.example.shared.type.PaymentMethod;
import org.example.shared.type.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 이벤트 DTO (Kafka 메시지)
 */
public record PaymentEvent(
        String eventId,
        PaymentStatus eventType,
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String transactionId,
        String failureReason,
        LocalDateTime eventTimestamp
) {
    /**
     * 결제 완료 이벤트 생성
     */
    public static PaymentEvent completed(
            String paymentId,
            String orderId,
            String userId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String transactionId
    ) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                PaymentStatus.PAYMENT_COMPLETED,
                paymentId,
                orderId,
                userId,
                amount,
                paymentMethod,
                transactionId,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * 결제 실패 이벤트 생성
     */
    public static PaymentEvent failed(
            String paymentId,
            String orderId,
            String userId,
            BigDecimal amount,
            String failureReason
    ) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                PaymentStatus.PAYMENT_FAILED,
                paymentId,
                orderId,
                userId,
                amount,
                null,
                null,
                failureReason,
                LocalDateTime.now()
        );
    }
}