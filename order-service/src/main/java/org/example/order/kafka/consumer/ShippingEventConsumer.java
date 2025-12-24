package org.example.order.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.Order;
import org.example.order.repository.OrderRepository;
import org.example.shared.dto.ShippingEvent;
import org.example.shared.exception.order.OrderNotFoundException;
import org.example.shared.type.order.OrderStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배송 이벤트 구독자
 * - 배송 상태 변경에 따라 주문 상태 업데이트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "shipping-event",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "shippingEventListenerFactory"
    )
    @Transactional
    public void consumeShippingEvent(ShippingEvent event, Acknowledgment ack) {
        try {
            log.info("배송 이벤트 수신 - eventType: {}, orderId: {}, shipmentId: {}, shippingStatus: {}",
                    event.eventType(), event.orderId(), event.shipmentId(), event.shippingStatus());

            switch (event.eventType()) {
                case SHIPPING_PREPARING -> handleShippingPreparing(event);
                case SHIPPING_STARTED -> handleShippingStarted(event);
                case SHIPPING_COMPLETED -> handleShippingCompleted(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }
            ack.acknowledge();

        } catch (Exception e) {
            log.error("배송 이벤트 처리 중 오류 - orderId: {}, shipmentId: {}",
                    event.orderId(), event.shipmentId(), e);
            ack.acknowledge();
        }
    }

    /**
     * 배송 준비 → 주문 상태를 SHIPPING_PREPARED로 변경
     */
    private void handleShippingPreparing(ShippingEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.updateStatus(OrderStatus.SHIPPING_PREPARED);
        orderRepository.save(order);

        log.info("배송 준비 완료 - orderId: {}, shipmentId: {}, status: SHIPPING_PREPARED",
                event.orderId(), event.shipmentId());
    }

    /**
     * 배송 시작 → 주문 상태를 SHIPPING_STARTED로 변경
     */
    private void handleShippingStarted(ShippingEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.updateStatus(OrderStatus.SHIPPING_STARTED);
        orderRepository.save(order);

        log.info("배송 시작 - orderId: {}, shipmentId: {}, trackingNumber: {}, carrier: {}, estimatedDelivery: {}",
                event.orderId(), event.shipmentId(), event.trackingNumber(),
                event.carrier(), event.estimatedDeliveryDate());
    }

    /**
     * 배송 완료 → 주문 상태를 SHIPPING_COMPLETED로 변경
     */
    private void handleShippingCompleted(ShippingEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.updateStatus(OrderStatus.SHIPPING_COMPLETED);
        orderRepository.save(order);

        log.info("배송 완료 - orderId: {}, shipmentId: {}, actualDelivery: {}",
                event.orderId(), event.shipmentId(), event.actualDeliveryDate());
    }
}