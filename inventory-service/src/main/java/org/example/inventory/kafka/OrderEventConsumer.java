package org.example.inventory.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.config.KafkaTopics;
import org.example.inventory.service.InventoryService;
import org.example.shared.dto.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 구독자
 * - OrderCreated 이벤트를 수신하여 재고 예약
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = KafkaTopics.ORDER_EVENT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(OrderEvent event, Acknowledgment ack) {
        try {
            log.info("주문 이벤트 수신 - eventType: {}, orderId: {}",
                    event.eventType(), event.orderId());

            switch (event.eventType()) {
                case ORDER_CREATED -> handleOrderCreated(event);
                case ORDER_CANCELLED -> handleOrderCancelled(event);
                case ORDER_STATUS_CHANGED -> handleOrderStatusChanged(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("주문 이벤트 처리 중 오류 발생 - orderId: {}", event.orderId(), e);
            // TODO: 재시도 로직 또는 Dead Letter Queue 처리
        }
    }

    /**
     * OrderCreated 이벤트 처리 → 재고 예약
     */
    private void handleOrderCreated(OrderEvent event) {
        log.info("주문 생성 이벤트 처리 - orderId: {}, userId: {}, items: {}",
                event.orderId(), event.userId(), event.items().size());

        inventoryService.reserveInventory(event);
    }

    /**
     * OrderCancelled 이벤트 처리 → 재고 복구
     */
    private void handleOrderCancelled(OrderEvent event) {
        log.info("주문 취소 이벤트 수신 - orderId: {}", event.orderId());
        // TODO: 주문 취소 시 재고 복구 로직 구현
    }

    /**
     * OrderStatusChanged 이벤트 처리
     */
    private void handleOrderStatusChanged(OrderEvent event) {
        log.info("주문 상태 변경 이벤트 수신 - orderId: {}", event.orderId());
        // TODO: 주문 상태 변경 처리
    }
}