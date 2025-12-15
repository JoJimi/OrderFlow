package org.example.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.dto.InventoryEvent;
import org.example.shared.dto.OrderEvent;
import org.example.shared.dto.PaymentEvent;
import org.example.shared.dto.ShippingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 이벤트 로그 Kafka Consumer
 * 모든 주문 관련 이벤트를 구독하여 DB에 저장합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogEventConsumer {

    private final EventLogService eventLogService;
    private final ObjectMapper objectMapper;

    /**
     * 주문 이벤트 구독
     * - OrderCreated, OrderCancelled
     */
    @KafkaListener(
            topics = "order-event",
            groupId = "event-logging-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.info("[Kafka] 주문 이벤트 수신 - Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);

        try {
            // OrderEvent로 파싱
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("[Kafka] 주문 이벤트 파싱 완료 - EventType: {}, OrderId: {}, EventId: {}",
                    event.eventType(), event.orderId(), event.eventId());

            // 이벤트 로그 저장
            eventLogService.saveEventLog(
                    event,
                    event.eventId(),
                    event.orderId(),
                    event.eventType().name(),
                    "order-service"
            );

            ack.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("[Kafka] 주문 이벤트 파싱 실패 - Message: {}", message, e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Kafka] 주문 이벤트 처리 중 예외 발생 - Message: {}", message, e);
            ack.acknowledge();
        }
    }

    /**
     * 결제 이벤트 구독
     * - PaymentPending, PaymentCompleted, PaymentFailed
     */
    @KafkaListener(
            topics = "payment-event",
            groupId = "event-logging-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.info("[Kafka] 결제 이벤트 수신 - Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);

        try {
            // PaymentEvent로 파싱
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            log.info("[Kafka] 결제 이벤트 파싱 완료 - EventType: {}, OrderId: {}, EventId: {}",
                    event.eventType(), event.orderId(), event.eventId());

            // 이벤트 로그 저장
            eventLogService.saveEventLog(
                    event,
                    event.eventId(),
                    event.orderId(),
                    event.eventType().name(),
                    "payment-service"
            );

            ack.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("[Kafka] 결제 이벤트 파싱 실패 - Message: {}", message, e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Kafka] 결제 이벤트 처리 중 예외 발생 - Message: {}", message, e);
            ack.acknowledge();
        }
    }

    /**
     * 배송 이벤트 구독
     * - ShippingPreparing, ShippingStarted, ShippingCompleted
     */
    @KafkaListener(
            topics = "shipping-event",
            groupId = "event-logging-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeShippingEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.info("[Kafka] 배송 이벤트 수신 - Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);

        try {
            // ShippingEvent로 파싱
            ShippingEvent event = objectMapper.readValue(message, ShippingEvent.class);
            log.info("[Kafka] 배송 이벤트 파싱 완료 - EventType: {}, OrderId: {}, EventId: {}",
                    event.eventType(), event.orderId(), event.eventId());

            // 이벤트 로그 저장
            eventLogService.saveEventLog(
                    event,
                    event.eventId(),
                    event.orderId(),
                    event.eventType().name(),
                    "shipping-service"
            );

            ack.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("[Kafka] 배송 이벤트 파싱 실패 - Message: {}", message, e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Kafka] 배송 이벤트 처리 중 예외 발생 - Message: {}", message, e);
            ack.acknowledge();
        }
    }

    /**
     * 재고 이벤트 구독
     * - InventoryReserved, InventoryDeducted, InventoryRestored
     */
    @KafkaListener(
            topics = "inventory-event",
            groupId = "event-logging-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeInventoryEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.info("[Kafka] 재고 이벤트 수신 - Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);

        try {
            // InventoryEvent로 파싱
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);
            log.info("[Kafka] 재고 이벤트 파싱 완료 - EventType: {}, OrderId: {}, EventId: {}",
                    event.eventType(), event.orderId(), event.eventId());

            // 이벤트 로그 저장
            eventLogService.saveEventLog(
                    event,
                    event.eventId(),
                    event.orderId(),
                    event.eventType().name(),
                    "inventory-service"
            );

            ack.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("[Kafka] 재고 이벤트 파싱 실패 - Message: {}", message, e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Kafka] 재고 이벤트 처리 중 예외 발생 - Message: {}", message, e);
            ack.acknowledge();
        }
    }
}