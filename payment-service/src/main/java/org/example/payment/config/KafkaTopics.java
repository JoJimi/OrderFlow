package org.example.payment.config;

public class KafkaTopics {

    public static final String PAYMENT_EVENT = "payment-event";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}