package org.example.payment.config;

/**
 * Kafka Topic 상수
 * Payment Service에서 사용하는 토픽들을 정의합니다.
 */
public class KafkaTopics {

    /**
     * 결제 이벤트 토픽
     * - 결제 완료, 실패 이벤트 발행
     * - Partition: 3
     * - Replication Factor: 1 (개발 환경)
     */
    public static final String PAYMENT_EVENT = "payment-event";

    private KafkaTopics() {
        // 유틸리티 클래스 - 인스턴스화 방지
        throw new UnsupportedOperationException("Utility class");
    }
}