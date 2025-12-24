package org.example.order.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.Order;
import org.example.order.repository.OrderRepository;
import org.example.shared.dto.InventoryEvent;
import org.example.shared.exception.order.OrderNotFoundException;
import org.example.shared.type.order.OrderStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "inventory-event",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryEventListenerFactory"
    )
    @Transactional
    public void consumeInventoryEvent(InventoryEvent event, Acknowledgment ack) {
        try {
            log.info("재고 이벤트 수신 - eventType: {}, orderId: {}, action: {}, reason: {}",
                    event.eventType(), event.orderId(), event.action(), event.reason());

            switch (event.eventType()) {
                case INVENTORY_RESERVED -> handleInventoryReserved(event);
                case INVENTORY_INSUFFICIENT -> handleInventoryInsufficient(event);
                case INVENTORY_DEDUCTED -> handleInventoryDeducted(event);
                case INVENTORY_RESTORED -> handleInventoryRestored(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }
            ack.acknowledge();

        } catch (Exception e) {
            log.error("재고 이벤트 처리 중 오류 - orderId: {}, reason: {}",
                    event.orderId(), event.reason(), e);
            ack.acknowledge();
        }
    }

    /**
     * 재고 예약 성공 → 주문 상태를 PAYMENT_PENDING으로 변경
     */
    private void handleInventoryReserved(InventoryEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.updateStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);

        log.info("재고 예약 성공 - orderId: {}, productId: {}, quantity: {}, reason: {}",
                event.orderId(), event.productId(), event.quantity(), event.reason());    }

    /**
     * 재고 부족 → 주문 취소
     */
    private void handleInventoryInsufficient(InventoryEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.cancel();
        orderRepository.save(order);

        log.warn("재고 부족으로 주문 취소 - orderId: {}, productId: {}, reason: {}",
                event.orderId(), event.productId(), event.reason());
    }

    /**
     * 재고 차감 완료 (결제 완료 후)
     */
    private void handleInventoryDeducted(InventoryEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.updateStatus(OrderStatus.PAYMENT_COMPLETED);
        orderRepository.save(order);

        log.info("재고 차감 완료 - orderId: {}, productId: {}, quantity: {}, action: {}",
                event.orderId(), event.productId(), event.quantity(), event.action());
    }

    /**
     * 재고 복구 완료 (결제 실패 후)
     */
    private void handleInventoryRestored(InventoryEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(OrderNotFoundException::new);

        order.updateStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        log.info("재고 복구 완료 - orderId: {}, productId: {}, quantity: {}, reason: {}",
                event.orderId(), event.productId(), event.quantity(), event.reason());
    }
}