package org.example.shipping.config.kafka;

public class KafkaTopics {

    public static final String SHIPPING_EVENT = "shipping-event";
    public static final String PAYMENT_EVENT = "payment-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}