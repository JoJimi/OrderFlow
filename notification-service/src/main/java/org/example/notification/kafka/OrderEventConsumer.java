package org.example.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.config.KafkaTopics;
import org.example.notification.service.NotificationService;
import org.example.shared.dto.OrderEvent;
import org.example.shared.type.notification.NotificationType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 구독자
 * - OrderCreated, OrderCancelled 이벤트를 수신하여 알림 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.ORDER_EVENT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderEventListenerFactory"
    )
    public void consumeOrderEvent(OrderEvent event, Acknowledgment ack) {
        try {
            log.info("주문 이벤트 수신 - eventType: {}, orderId: {}",
                    event.eventType(), event.orderId());

            switch (event.eventType()) {
                case ORDER_CREATED -> handleOrderCreated(event);
                case ORDER_CANCELLED -> handleOrderCancelled(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("주문 이벤트 처리 중 오류 발생 - eventType: {}, orderId: {}, eventId: {}",
                    event.eventType(), event.orderId(), event.eventId(), e);
            ack.acknowledge();
        }
    }

    /**
     * OrderCreated 이벤트 처리 → 주문 생성 알림
     */
    private void handleOrderCreated(OrderEvent event) {
        log.info("주문 생성 이벤트 처리 - orderId: {}, userId: {}",
                event.orderId(), event.userId());

        String message = String.format(
                "주문이 생성되었습니다. 주문번호: %s, 금액: %s원",
                event.orderId(),
                event.totalPrice()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.ORDER,
                event.orderId()
        );
    }

    /**
     * OrderCancelled 이벤트 처리 → 주문 취소 알림
     */
    private void handleOrderCancelled(OrderEvent event) {
        log.info("주문 취소 이벤트 처리 - orderId: {}, userId: {}",
                event.orderId(), event.userId());

        String message = String.format(
                "주문이 취소되었습니다. 주문번호: %s",
                event.orderId()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.ORDER,
                event.orderId()
        );
    }
}