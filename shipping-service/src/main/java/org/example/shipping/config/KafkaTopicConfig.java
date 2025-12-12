package org.example.shipping.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic 설정
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * shipping-event 토픽 생성
     */
    @Bean
    public NewTopic shippingEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.SHIPPING_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7일
                .config("compression.type", "gzip")
                .build();
    }
}