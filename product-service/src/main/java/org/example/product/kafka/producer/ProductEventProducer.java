package org.example.product.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.config.kafka.KafkaTopics;
import org.example.shared.dto.ProductEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 상품 생성 이벤트 발행
     */
    public void publishProductCreatedEvent(ProductEvent event) {
        publishEvent(event, "상품 생성");
    }

    /**
     * 상품 수정 이벤트 발행
     */
    public void publishProductUpdatedEvent(ProductEvent event) {
        publishEvent(event, "상품 수정");
    }

    /**
     * 상품 삭제 이벤트 발행
     */
    public void publishProductDeletedEvent(ProductEvent event) {
        publishEvent(event, "상품 삭제");
    }

    /**
     * 상품 대량 생성 이벤트 발행
     */
    public void publishBulkCreatedEvent(int count) {
        ProductEvent event = ProductEvent.bulkCreated(count);
        publishEvent(event, "상품 대량 생성");
    }

    /**
     * Kafka로 이벤트를 발행하는 공통 메서드
     */
    private void publishEvent(ProductEvent event, String eventTypeDescription) {
        try {
            // 메시지 키: BULK_CREATED는 "bulk", 개별 상품은 productId
            String messageKey = event.productId() != null ? event.productId() : "bulk";

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaTopics.PRODUCT_EVENT,
                    messageKey,
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[Kafka] {} 이벤트 발행 성공 - ProductId: {}, EventId: {}, Partition: {}, Offset: {}",
                            eventTypeDescription,
                            event.productId() != null ? event.productId() : "BULK",
                            event.eventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                } else {
                    log.error("[Kafka] {} 이벤트 발행 실패 - ProductId: {}, EventId: {}, Error: {}",
                            eventTypeDescription,
                            event.productId() != null ? event.productId() : "BULK",
                            event.eventId(),
                            ex.getMessage(),
                            ex
                    );
                }
            });

        } catch (Exception e) {
            log.error("[Kafka] {} 이벤트 발행 중 예외 발생 - ProductId: {}, EventId: {}",
                    eventTypeDescription,
                    event.productId() != null ? event.productId() : "BULK",
                    event.eventId(),
                    e
            );
        }
    }
}