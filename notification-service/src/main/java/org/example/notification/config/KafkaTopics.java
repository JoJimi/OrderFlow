package org.example.notification.config;

/**
 * Kafka Topic 상수
 */
public class KafkaTopics {

    /**
     * 주문 이벤트 토픽 (구독용)
     */
    public static final String ORDER_EVENT = "order-event";

    /**
     * 결제 이벤트 토픽 (구독용)
     */
    public static final String PAYMENT_EVENT = "payment-event";

    /**
     * 배송 이벤트 토픽 (구독용)
     */
    public static final String SHIPPING_EVENT = "shipping-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}