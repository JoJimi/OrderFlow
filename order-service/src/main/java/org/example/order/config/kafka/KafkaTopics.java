package org.example.order.config.kafka;

/**
 * Kafka Topic 상수
 * Order Service에서 사용하는 토픽들을 정의합니다.
 */
public class KafkaTopics {

    public static final String ORDER_EVENT = "order-event";
    public static final String ORDER_STATUS_EVENT = "order-status-event";

    private KafkaTopics() {

    }
}