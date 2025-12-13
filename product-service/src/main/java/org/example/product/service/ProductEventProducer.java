package org.example.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.config.KafkaTopics;
import org.example.shared.dto.ProductEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Product 이벤트 발행 서비스
 * Kafka를 통해 상품 관련 이벤트를 다른 서비스에 전파합니다.
 */
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
    public void publishProductBulkCreatedEvent(ProductEvent event) {
        publishEvent(event, "상품 대량 생성");
    }

    /**
     * Kafka로 이벤트를 발행하는 공통 메서드
     */
    private void publishEvent(ProductEvent event, String eventTypeDescription) {
        try {
            // 비동기로 메시지 전송
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaTopics.PRODUCT_EVENT,
                    event.productId(),  // 메시지 키 (파티셔닝에 사용)
                    event               // 메시지 값
            );

            // 전송 성공 콜백
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[Kafka] {} 이벤트 발행 성공 - ProductId: {}, EventId: {}, Partition: {}, Offset: {}",
                            eventTypeDescription,
                            event.productId(),
                            event.eventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                } else {
                    log.error("[Kafka] {} 이벤트 발행 실패 - ProductId: {}, EventId: {}, Error: {}",
                            eventTypeDescription,
                            event.productId(),
                            event.eventId(),
                            ex.getMessage(),
                            ex
                    );
                }
            });

        } catch (Exception e) {
            log.error("[Kafka] {} 이벤트 발행 중 예외 발생 - ProductId: {}, EventId: {}",
                    eventTypeDescription,
                    event.productId(),
                    event.eventId(),
                    e
            );
        }
    }

    /**
     * 동기식 이벤트 발행 (테스트용)
     * 프로덕션에서는 비동기 방식을 권장합니다.
     */
    public void publishEventSync(ProductEvent event) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(
                    KafkaTopics.PRODUCT_EVENT,
                    event.productId(),
                    event
            ).get();

            log.info("[Kafka] 동기 이벤트 발행 성공 - ProductId: {}, Partition: {}, Offset: {}",
                    event.productId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );

        } catch (Exception e) {
            log.error("[Kafka] 동기 이벤트 발행 실패 - ProductId: {}, Error: {}",
                    event.productId(),
                    e.getMessage(),
                    e
            );
            throw new RuntimeException("Kafka 이벤트 발행 실패", e);
        }
    }
}