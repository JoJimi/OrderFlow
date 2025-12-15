package org.example.shared.dto;

import org.example.shared.type.payment.PaymentEventType;
import org.example.shared.type.payment.PaymentMethod;
import org.example.shared.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentEvent(
        String eventId,
        PaymentEventType eventType,
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String transactionId,
        String failureReason,
        LocalDateTime eventTimestamp
) {

    public record OrderItemInfo(
            String productId,
            Integer quantity
    ) {}

    public static PaymentEvent completed(
            String paymentId,
            String orderId,
            String userId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String transactionId
    ) {
        return new PaymentEvent(
                IdGenerator.generateEventId(),
                PaymentEventType.PAYMENT_COMPLETED,
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

    public static PaymentEvent failed(
            String paymentId,
            String orderId,
            String userId,
            BigDecimal amount,
            String failureReason
    ) {
        return new PaymentEvent(
                IdGenerator.generateEventId(),
                PaymentEventType.PAYMENT_FAILED,
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

    public static PaymentEvent cancelled(
            String paymentId,
            String orderId,
            String userId,
            BigDecimal amount
    ) {
        return new PaymentEvent(
                IdGenerator.generateEventId(),
                PaymentEventType.PAYMENT_CANCELLED,
                paymentId,
                orderId,
                userId,
                amount,
                null,
                null,
                "주문 취소로 인한 결제 취소",
                LocalDateTime.now()
        );
    }
}