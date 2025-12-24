package org.example.notification.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.config.kafka.KafkaTopics;
import org.example.notification.service.NotificationService;
import org.example.shared.dto.PaymentEvent;
import org.example.shared.type.notification.NotificationType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트 구독자
 * - PaymentCompleted, PaymentFailed 이벤트를 수신하여 알림 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

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
            log.error("결제 이벤트 처리 중 오류 발생 - eventType: {}, orderId: {}, eventId: {}",
                    event.eventType(), event.orderId(), event.eventId(), e);
            ack.acknowledge();
        }
    }

    /**
     * PaymentCompleted 이벤트 처리 → 결제 완료 알림
     */
    private void handlePaymentCompleted(PaymentEvent event) {
        log.info("결제 완료 이벤트 처리 - orderId: {}, paymentId: {}",
                event.orderId(), event.paymentId());

        String message = String.format(
                "결제가 완료되었습니다. 주문번호: %s, 결제금액: %s원, 결제수단: %s",
                event.orderId(),
                event.amount(),
                event.paymentMethod().getDescription()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.PAYMENT,
                event.orderId()
        );
    }

    /**
     * PaymentFailed 이벤트 처리 → 결제 실패 알림
     */
    private void handlePaymentFailed(PaymentEvent event) {
        log.info("결제 실패 이벤트 처리 - orderId: {}, reason: {}",
                event.orderId(), event.failureReason());

        String message = String.format(
                "결제가 실패했습니다. 주문번호: %s, 실패사유: %s",
                event.orderId(),
                event.failureReason()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.PAYMENT,
                event.orderId()
        );
    }
}