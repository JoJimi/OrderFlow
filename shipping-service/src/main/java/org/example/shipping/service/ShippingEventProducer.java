package org.example.shipping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shipping.config.KafkaTopics;
import org.example.shipping.domain.Shipment;
import org.example.shared.dto.ShippingEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 배송 이벤트 발행자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * ShippingPreparing 이벤트 발행
     */
    public void publishShippingPreparingEvent(String shipmentId, String orderId, String userId) {
        ShippingEvent event = ShippingEvent.preparing(shipmentId, orderId, userId);
        publishEvent(event);
    }

    /**
     * ShippingStarted 이벤트 발행
     */
    public void publishShippingStartedEvent(Shipment shipment) {
        ShippingEvent event = ShippingEvent.started(
                shipment.getShipmentId(),
                shipment.getOrderId(),
                shipment.getUserId(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getEstimatedDeliveryDate()
        );
        publishEvent(event);
    }

    /**
     * ShippingCompleted 이벤트 발행
     */
    public void publishShippingCompletedEvent(Shipment shipment) {
        ShippingEvent event = ShippingEvent.completed(
                shipment.getShipmentId(),
                shipment.getOrderId(),
                shipment.getUserId(),
                shipment.getTrackingNumber(),
                shipment.getActualDeliveryDate()
        );
        publishEvent(event);
    }

    /**
     * Kafka 이벤트 발행
     */
    private void publishEvent(ShippingEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.SHIPPING_EVENT, event.orderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배송 이벤트 발행 성공 - eventType: {}, shipmentId: {}, orderId: {}",
                        event.eventType(), event.shipmentId(), event.orderId());
            } else {
                log.error("배송 이벤트 발행 실패 - eventType: {}, shipmentId: {}",
                        event.eventType(), event.shipmentId(), ex);
            }
        });
    }
}