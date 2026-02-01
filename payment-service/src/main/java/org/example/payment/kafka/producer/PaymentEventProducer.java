package org.example.payment.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.config.kafka.KafkaTopics;
import org.example.payment.domain.Payment;
import org.example.shared.dto.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 결제 이벤트 발행자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.completed(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionId()
        );

        publishEvent(event, "결제 완료");
    }

    public void publishPaymentCancelledEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.cancelled(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount()
        );

        publishEvent(event, "결제 취소");
    }

    public void publishPaymentFailedEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.failed(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getFailureReason()
        );

        publishEvent(event, "결제 실패");
    }

    private void publishEvent(PaymentEvent event, String eventDescription) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.PAYMENT_EVENT, event.orderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[Kafka] {} 이벤트 발행 성공 - paymentId: {}, orderId: {}, eventType: {}, partition: {}, offset: {}",
                        eventDescription,
                        event.paymentId(),
                        event.orderId(),
                        event.eventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("[Kafka] {} 이벤트 발행 실패 - paymentId: {}, orderId: {}",
                        eventDescription,
                        event.paymentId(),
                        event.orderId(),
                        ex);
            }
        });
    }
}