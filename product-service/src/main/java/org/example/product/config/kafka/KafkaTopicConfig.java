package org.example.product.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic 설정
 * 애플리케이션 시작 시 필요한 토픽들을 자동으로 생성합니다.
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * product-event 토픽 생성
     * - Partition: 3 (병렬 처리를 위해)
     * - Replication Factor: 1 (개발 환경, 프로덕션에서는 3 권장)
     * - Retention: 7일 (604800000 ms)
     */
    @Bean
    public NewTopic productEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.PRODUCT_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7일
                .config("compression.type", "gzip")
                .build();
    }

    /**
     * product-inventory-event 토픽 생성 (향후 재고 관리용)
     */
    @Bean
    public NewTopic productInventoryEventTopic() {
        return TopicBuilder
                .name(KafkaTopics.PRODUCT_INVENTORY_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")
                .config("compression.type", "gzip")
                .build();
    }
}