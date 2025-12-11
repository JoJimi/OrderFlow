package org.example.order.config;

/**
 * Kafka Topic 상수
 * Order Service에서 사용하는 토픽들을 정의합니다.
 */
public class KafkaTopics {

    /**
     * 주문 이벤트 토픽
     * - 주문 생성, 취소 이벤트 발행
     * - Partition: 3
     * - Replication Factor: 1 (개발 환경)
     */
    public static final String ORDER_EVENT = "order-event";

    /**
     * 주문 상태 변경 이벤트 토픽
     * - 주문 상태 변경 이벤트 발행
     */
    public static final String ORDER_STATUS_EVENT = "order-status-event";

    private KafkaTopics() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }
}