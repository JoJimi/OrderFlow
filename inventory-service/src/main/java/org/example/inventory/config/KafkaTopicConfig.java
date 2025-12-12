package org.example.inventory.config;

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
     * inventory-event 토픽 생성
     */
    @Bean
    public NewTopic inventoryEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.INVENTORY_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7일
                .config("compression.type", "gzip")
                .build();
    }
}