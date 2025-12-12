package org.example.shipping.config;

/**
 * Kafka Topic 상수
 */
public class KafkaTopics {

    /**
     * 배송 이벤트 토픽
     */
    public static final String SHIPPING_EVENT = "shipping-event";

    /**
     * 결제 이벤트 토픽 (구독용)
     */
    public static final String PAYMENT_EVENT = "payment-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}