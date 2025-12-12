package org.example.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.config.KafkaTopics;
import org.example.shared.dto.InventoryEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 재고 이벤트 발행자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 재고 예약 이벤트 발행
     */
    public void publishInventoryReservedEvent(String inventoryId, String productId,
                                              String orderId, Integer quantity) {
        InventoryEvent event = InventoryEvent.reserved(inventoryId, productId, orderId, quantity);
        publishEvent(event, orderId);
    }

    /**
     * 재고 부족 이벤트 발행
     */
    public void publishInventoryInsufficientEvent(String productId, String orderId,
                                                  Integer requestedQuantity, Integer availableStock) {
        InventoryEvent event = InventoryEvent.insufficient(productId, orderId,
                requestedQuantity, availableStock);
        publishEvent(event, orderId);
    }

    /**
     * 재고 차감 이벤트 발행
     */
    public void publishInventoryDeductedEvent(String inventoryId, String productId,
                                              String orderId, Integer quantity) {
        InventoryEvent event = InventoryEvent.deducted(inventoryId, productId, orderId, quantity);
        publishEvent(event, orderId);
    }

    /**
     * 재고 복구 이벤트 발행
     */
    public void publishInventoryRestoredEvent(String inventoryId, String productId,
                                              String orderId, Integer quantity) {
        InventoryEvent event = InventoryEvent.restored(inventoryId, productId, orderId, quantity);
        publishEvent(event, orderId);
    }

    /**
     * Kafka 이벤트 발행
     */
    private void publishEvent(InventoryEvent event, String key) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.INVENTORY_EVENT, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("재고 이벤트 발행 성공 - eventType: {}, productId: {}, orderId: {}",
                        event.eventType(), event.productId(), event.orderId());
            } else {
                log.error("재고 이벤트 발행 실패 - eventType: {}, productId: {}",
                        event.eventType(), event.productId(), ex);
            }
        });
    }
}