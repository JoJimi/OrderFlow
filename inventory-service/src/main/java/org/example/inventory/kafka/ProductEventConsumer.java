package org.example.inventory.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.config.KafkaTopics;
import org.example.inventory.service.InventoryService;
import org.example.shared.dto.ProductEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 상품 이벤트 구독자
 * - ProductCreated 이벤트를 수신하여 재고 초기화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_EVENT,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeProductEvent(ProductEvent event, Acknowledgment ack) {
        try {
            log.info("상품 이벤트 수신 - eventType: {}, productId: {}",
                    event.eventType(), event.productId());

            switch (event.eventType()) {
                case PRODUCT_CREATED -> handleProductCreated(event);
                case PRODUCT_BULK_CREATED -> handleProductBulkCreated(event);
                case PRODUCT_UPDATED -> log.info("상품 수정 이벤트 - productId: {}", event.productId());
                case PRODUCT_DELETED -> log.info("상품 삭제 이벤트 - productId: {}", event.productId());
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("상품 이벤트 처리 중 오류 발생 - productId: {}", event.productId(), e);
            // TODO: 재시도 로직 또는 Dead Letter Queue 처리
        }
    }

    /**
     * ProductCreated 이벤트 처리 → 재고 초기화
     */
    private void handleProductCreated(ProductEvent event) {
        log.info("상품 생성 이벤트 처리 - productId: {}, stockQuantity: {}",
                event.productId(), event.stockQuantity());

        inventoryService.initializeInventory(event);
    }

    /**
     * ProductBulkCreated 이벤트 처리 → 대량 재고 초기화
     */
    private void handleProductBulkCreated(ProductEvent event) {
        log.info("대량 상품 생성 이벤트 수신 - count: {}", event.count());
        // 대량 생성 시에는 개별 ProductCreated 이벤트가 별도로 발행됨
    }
}