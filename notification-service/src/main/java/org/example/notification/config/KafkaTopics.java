package org.example.notification.config;

public class KafkaTopics {

    public static final String ORDER_EVENT = "order-event";
    public static final String PAYMENT_EVENT = "payment-event";
    public static final String SHIPPING_EVENT = "shipping-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}