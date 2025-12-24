package org.example.order.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.config.kafka.KafkaTopics;
import org.example.shared.dto.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Order 이벤트 발행 서비스
 * Kafka를 통해 주문 관련 이벤트를 다른 서비스에 전파합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 주문 생성 이벤트 발행
     */
    public void publishOrderCreatedEvent(OrderEvent event) {
        publishEvent(event, "주문 생성", KafkaTopics.ORDER_EVENT);
    }

    /**
     * 주문 취소 이벤트 발행
     */
    public void publishOrderCancelledEvent(OrderEvent event) {
        publishEvent(event, "주문 취소", KafkaTopics.ORDER_EVENT);
    }

    /**
     * 주문 상태 변경 이벤트 발행
     */
    public void publishOrderStatusChangedEvent(OrderEvent event) {
        publishEvent(event, "주문 상태 변경", KafkaTopics.ORDER_STATUS_EVENT);
    }

    /**
     * Kafka로 이벤트를 발행하는 공통 메서드
     */
    private void publishEvent(OrderEvent event, String eventTypeDescription, String topic) {
        try {
            // 비동기로 메시지 전송
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    topic,
                    event.orderId(),  // 메시지 키 (파티셔닝에 사용)
                    event             // 메시지 값
            );

            // 전송 성공 콜백
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[Kafka] {} 이벤트 발행 성공 - OrderId: {}, EventId: {}, Partition: {}, Offset: {}",
                            eventTypeDescription,
                            event.orderId(),
                            event.eventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                } else {
                    log.error("[Kafka] {} 이벤트 발행 실패 - OrderId: {}, EventId: {}, Error: {}",
                            eventTypeDescription,
                            event.orderId(),
                            event.eventId(),
                            ex.getMessage(),
                            ex
                    );
                }
            });

        } catch (Exception e) {
            log.error("[Kafka] {} 이벤트 발행 중 예외 발생 - OrderId: {}, EventId: {}",
                    eventTypeDescription,
                    event.orderId(),
                    event.eventId(),
                    e
            );
        }
    }
}