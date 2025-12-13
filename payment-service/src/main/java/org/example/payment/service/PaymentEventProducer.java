package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.config.KafkaTopics;
import org.example.payment.domain.Payment;
import org.example.shared.dto.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 결제 이벤트 발행자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * PaymentCompleted 이벤트 발행
     */
    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.completed(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionId()
        );

        publishEvent(event);
    }

    /**
     * PaymentFailed 이벤트 발행
     */
    public void publishPaymentFailedEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.failed(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getFailureReason()
        );

        publishEvent(event);
    }

    /**
     * Kafka 이벤트 발행
     */
    private void publishEvent(PaymentEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.PAYMENT_EVENT, event.orderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 이벤트 발행 성공 - eventType: {}, paymentId: {}, orderId: {}",
                        event.eventType(), event.paymentId(), event.orderId());
            } else {
                log.error("결제 이벤트 발행 실패 - eventType: {}, paymentId: {}",
                        event.eventType(), event.paymentId(), ex);
            }
        });
    }
}