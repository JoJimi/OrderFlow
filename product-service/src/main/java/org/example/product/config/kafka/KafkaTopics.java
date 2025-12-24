package org.example.product.config.kafka;

/**
 * Kafka Topic 상수
 * Product Service에서 사용하는 토픽들을 정의합니다.
 */
public class KafkaTopics {

    /**
     * 상품 이벤트 토픽
     * - 상품 생성, 수정, 삭제 이벤트 발행
     * - Partition: 3
     * - Replication Factor: 1 (개발 환경)
     */
    public static final String PRODUCT_EVENT = "product-event";

    /**
     * 상품 재고 이벤트 토픽 (향후 사용)
     */
    public static final String PRODUCT_INVENTORY_EVENT = "product-inventory-event";

    private KafkaTopics() { }
}