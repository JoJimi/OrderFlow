package org.example.inventory.config;

/**
 * Kafka Topic 상수
 */
public class KafkaTopics {

    /**
     * 재고 이벤트 토픽
     */
    public static final String INVENTORY_EVENT = "inventory-event";

    /**
     * 상품 이벤트 토픽 (구독용)
     */
    public static final String PRODUCT_EVENT = "product-event";

    /**
     * 주문 이벤트 토픽 (구독용)
     */
    public static final String ORDER_EVENT = "order-event";

    /**
     * 결제 이벤트 토픽 (구독용)
     */
    public static final String PAYMENT_EVENT = "payment-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}