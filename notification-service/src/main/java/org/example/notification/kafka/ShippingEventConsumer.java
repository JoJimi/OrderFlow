package org.example.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.config.KafkaTopics;
import org.example.notification.service.NotificationService;
import org.example.shared.dto.ShippingEvent;
import org.example.shared.type.notification.NotificationType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 배송 이벤트 구독자
 * - ShippingPreparing, ShippingStarted, ShippingCompleted 이벤트를 수신하여 알림 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.SHIPPING_EVENT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeShippingEvent(ShippingEvent event, Acknowledgment ack) {
        try {
            log.info("배송 이벤트 수신 - eventType: {}, orderId: {}",
                    event.eventType(), event.orderId());

            switch (event.eventType()) {
                case SHIPPING_PREPARING -> handleShippingPreparing(event);
                case SHIPPING_STARTED -> handleShippingStarted(event);
                case SHIPPING_COMPLETED -> handleShippingCompleted(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("배송 이벤트 처리 중 오류 발생 - orderId: {}", event.orderId(), e);
            // TODO: 재시도 로직 또는 Dead Letter Queue 처리
        }
    }

    /**
     * ShippingPreparing 이벤트 처리 → 배송 준비 알림
     */
    private void handleShippingPreparing(ShippingEvent event) {
        log.info("배송 준비 이벤트 처리 - orderId: {}, shipmentId: {}",
                event.orderId(), event.shipmentId());

        String message = String.format(
                "배송 준비가 시작되었습니다. 주문번호: %s",
                event.orderId()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.SHIPPING,
                event.orderId()
        );
    }

    /**
     * ShippingStarted 이벤트 처리 → 배송 시작 알림
     */
    private void handleShippingStarted(ShippingEvent event) {
        log.info("배송 시작 이벤트 처리 - orderId: {}, trackingNumber: {}",
                event.orderId(), event.trackingNumber());

        String message = String.format(
                "배송이 시작되었습니다. 주문번호: %s, 운송장번호: %s, 택배사: %s, 예상배송일: %s",
                event.orderId(),
                event.trackingNumber(),
                event.carrier(),
                event.estimatedDeliveryDate()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.SHIPPING,
                event.orderId()
        );
    }

    /**
     * ShippingCompleted 이벤트 처리 → 배송 완료 알림
     */
    private void handleShippingCompleted(ShippingEvent event) {
        log.info("배송 완료 이벤트 처리 - orderId: {}, shipmentId: {}",
                event.orderId(), event.shipmentId());

        String message = String.format(
                "배송이 완료되었습니다. 주문번호: %s, 배송완료시각: %s",
                event.orderId(),
                event.actualDeliveryDate()
        );

        notificationService.createAndSendNotification(
                event.userId(),
                message,
                NotificationType.SHIPPING,
                event.orderId()
        );
    }
}