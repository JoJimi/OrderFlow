package org.example.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.service.PaymentService;
import org.example.shared.dto.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 구독자
 * - OrderCreated 이벤트를 수신하여 결제 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @Value("${kafka.consumer.processing-delay-ms:10}")
    private long processingDelayMs;

    @KafkaListener(
            topics = "${kafka.topics.order-event}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(OrderEvent event, Acknowledgment ack) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("주문 이벤트 수신 - eventType: {}, orderId: {}",
                    event.eventType(), event.orderId());

            // 처리 시간 시뮬레이션 (DB 저장, 외부 API 호출 등을 모방)
            if (processingDelayMs > 0) {
                Thread.sleep(processingDelayMs);
            }

            switch (event.eventType()) {
                case ORDER_CREATED -> handleOrderCreated(event);
                case ORDER_CANCELLED -> handleOrderCancelled(event);
                default -> log.warn("처리되지 않은 이벤트 타입: {}", event.eventType());
            }
            ack.acknowledge();

            long processingTime = System.currentTimeMillis() - startTime;
            log.debug("이벤트 처리 완료 - orderId: {}, 처리시간: {}ms",
                    event.orderId(), processingTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("주문 이벤트 처리 중 인터럽트 발생 - orderId: {}", event.orderId(), e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("주문 이벤트 처리 중 오류 발생 - orderId: {}", event.orderId(), e);
            ack.acknowledge();
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        log.info("주문 생성 이벤트 처리 - orderId: {}, userId: {}, totalPrice: {}",
                event.orderId(), event.userId(), event.totalPrice());

        paymentService.createPaymentRecord(
                event.orderId(),
                event.userId(),
                event.totalPrice()
        );
    }

    private void handleOrderCancelled(OrderEvent event) {
        log.info("주문 취소 이벤트 수신 - orderId: {}", event.orderId());
        paymentService.cancelPayment(event.orderId());
    }
}