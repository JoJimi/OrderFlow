package org.example.shipping.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

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