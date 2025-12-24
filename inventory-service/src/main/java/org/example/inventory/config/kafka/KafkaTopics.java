package org.example.inventory.config.kafka;

public class KafkaTopics {

    public static final String INVENTORY_EVENT = "inventory-event";
    public static final String PRODUCT_EVENT = "product-event";
    public static final String ORDER_EVENT = "order-event";
    public static final String PAYMENT_EVENT = "payment-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}