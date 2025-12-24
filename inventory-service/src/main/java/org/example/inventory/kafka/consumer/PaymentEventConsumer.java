package org.example.inventory.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.config.kafka.KafkaTopics;
import org.example.inventory.service.InventoryService;
import org.example.shared.dto.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트 구독자
 * - PaymentCompleted: 재고 차감
 * - PaymentFailed: 재고 복구
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentEventListenerFactory"
    )
    public void consumePaymentEvent(PaymentEvent event, Acknowledgment ack) {
        try {
            log.info("결제 이벤트 수신 - eventType: {}, orderId: {}",
                    event.eventType(), event.orderId());

            switch (event.eventType()) {
                case PAYMENT_COMPLETED -> handlePaymentCompleted(event);
                case PAYMENT_FAILED -> handlePaymentFailed(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }
            ack.acknowledge();

        } catch (Exception e) {
            log.error("결제 이벤트 처리 중 오류 발생 - orderId: {}", event.orderId(), e);
            ack.acknowledge();
        }
    }

    /**
     * PaymentCompleted 이벤트 처리 → 재고 차감
     */
    private void handlePaymentCompleted(PaymentEvent event) {
        log.info("결제 완료 이벤트 처리 - orderId: {}, paymentId: {}",
                event.orderId(), event.paymentId());

        inventoryService.deductInventory(event);
    }

    /**
     * PaymentFailed 이벤트 처리 → 재고 복구
     */
    private void handlePaymentFailed(PaymentEvent event) {
        log.info("결제 실패 이벤트 처리 - orderId: {}, reason: {}",
                event.orderId(), event.failureReason());

        inventoryService.restoreInventory(event);
    }
}