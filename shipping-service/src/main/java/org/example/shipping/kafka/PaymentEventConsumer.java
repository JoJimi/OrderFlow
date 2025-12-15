package org.example.shipping.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shipping.config.KafkaTopics;
import org.example.shipping.service.ShippingService;
import org.example.shared.dto.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트 구독자
 * - PaymentCompleted 이벤트를 수신하여 배송 준비
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final ShippingService shippingService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(PaymentEvent event, Acknowledgment ack) {
        try {
            log.info("결제 이벤트 수신 - eventType: {}, orderId: {}",
                    event.eventType(), event.orderId());

            switch (event.eventType()) {
                case PAYMENT_COMPLETED -> handlePaymentCompleted(event);
                case PAYMENT_FAILED -> handlePaymentFailed(event);
                case PAYMENT_CANCELLED -> handlePaymentCancelled(event); // ✅ 추가
                default -> log.debug("처리되지 않은 이벤트 타입: {}", event.eventType());
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("결제 이벤트 처리 중 오류 발생 - orderId: {}", event.orderId(), e);

            ack.acknowledge();
        }
    }

    /**
     * PaymentCompleted 이벤트 처리 → 배송 준비
     */
    private void handlePaymentCompleted(PaymentEvent event) {
        log.info("결제 완료 이벤트 처리 - orderId: {}, paymentId: {}",
                event.orderId(), event.paymentId());

        shippingService.prepareShipping(event);
    }

    /**
     * PaymentFailed 이벤트 처리 (로그만)
     */
    private void handlePaymentFailed(PaymentEvent event) {
        log.info("결제 실패 이벤트 - orderId: {}, reason: {}",
                event.orderId(), event.failureReason());
    }

    /**
     * PaymentCancelled 이벤트 처리 (배송 취소)
     */
    private void handlePaymentCancelled(PaymentEvent event) {
        log.info("결제 취소 이벤트 - orderId: {}", event.orderId());
    }
}