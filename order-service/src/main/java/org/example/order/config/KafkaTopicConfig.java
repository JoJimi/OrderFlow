package org.example.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.ORDER_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")
                .config("compression.type", "gzip")
                .build();
    }

    @Bean
    public NewTopic orderStatusEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.ORDER_STATUS_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")
                .config("compression.type", "gzip")
                .build();
    }
}