package org.example.inventory.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.config.KafkaTopics;
import org.example.inventory.service.InventoryService;
import org.example.shared.dto.ProductEvent;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.inventory.InventoryNotFoundException;
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
                case PRODUCT_UPDATED -> handleProductUpdated(event);
                case PRODUCT_DELETED -> handleProductDeleted(event);
                case PRODUCT_BULK_CREATED -> handleProductBulkCreated(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }
            ack.acknowledge();

        } catch (Exception e) {
            log.error("상품 이벤트 처리 중 오류 발생 - productId: {}", event.productId(), e);

            ack.acknowledge();
        }
    }

    /**
     * ProductCreated 이벤트 처리 → 재고 초기화
     */
    private void handleProductCreated(ProductEvent event) {
        log.info("상품 생성 이벤트 처리 - productId: {}", event.productId());

        // stockQuantity 제거 - Inventory Service가 기본값으로 초기화
        inventoryService.initializeInventory(event);
    }


    /**
     * ProductBulkCreated 이벤트 처리 → 대량 재고 초기화
     */
    private void handleProductBulkCreated(ProductEvent event) {
        log.info("상품 대량 생성 이벤트 처리 - metadata: {}", event.metadata());

        try {
            int count = Integer.parseInt(event.metadata());

            log.info("대량 재고 초기화 시작 - count: {}", count);
            inventoryService.bulkInitializeInventory(count);
            log.info("대량 재고 초기화 완료 - count: {}", count);

        } catch (NumberFormatException e) {
            log.error("대량 생성 이벤트의 metadata 파싱 실패 - metadata: {}",
                    event.metadata(), e);
        } catch (Exception e) {
            log.error("대량 재고 초기화 중 오류 발생", e);
            throw e;  // 재시도 필요한 경우
        }
    }

    /**
     * ProductUpdated 이벤트 처리
     */
    private void handleProductUpdated(ProductEvent event) {
        log.info("상품 수정 이벤트 - productId: {}", event.productId());
        // 재고 관련 로직은 없지만, 필요시 상품 정보 캐시 갱신 등 가능
    }

    /**
     * ProductDeleted 이벤트 처리 → 재고 논리 삭제
     */
    private void handleProductDeleted(ProductEvent event) {
        log.info("상품 삭제 이벤트 처리 시작 - productId: {}", event.productId());

        try {
            inventoryService.markInventoryAsDeleted(
                    event.productId(),
                    "상품 삭제로 인한 재고 제거"
            );

            log.info("상품 삭제 이벤트 처리 완료 - productId: {}", event.productId());

        } catch (InventoryNotFoundException e) {
            // 재고가 없는 경우 - 경고만 남기고 진행
            log.warn("재고 정보가 존재하지 않아 삭제를 건너뜁니다 - productId: {}",
                    event.productId());

        } catch (BusinessException e) {
            // 예약 재고가 있는 경우 - 에러 로그 남기고 예외 전파
            log.error("재고 삭제 실패 (비즈니스 오류) - productId: {}, error: {}, code: {}",
                    event.productId(), e.getMessage(), e.getErrorCode());

            // 실제 운영에서는 DLQ(Dead Letter Queue)로 전송하거나
            // 보상 트랜잭션 필요
            // 현재는 로그만 남기고 ack 처리 (외부 catch에서)

        } catch (Exception e) {
            log.error("상품 삭제 이벤트 처리 중 예상치 못한 오류 - productId: {}",
                    event.productId(), e);
            throw e;
        }
    }
}