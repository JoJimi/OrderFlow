package org.example.user.config.performance;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 커스텀 메트릭 설정
 */
@Configuration
public class MetricsConfig {

    /**
     * 성능 측정용 타이머 등록
     */
    @Bean
    public Timer performanceTestTimer(MeterRegistry registry) {
        return Timer.builder("performance.test")
                .description("Performance test execution timer")
                .tag("type", "benchmark")
                .register(registry);
    }

    /**
     * Redis 작업 타이머
     */
    @Bean
    public Timer redisOperationTimer(MeterRegistry registry) {
        return Timer.builder("redis.operation")
                .description("Redis operation timer")
                .tag("operation", "get")
                .register(registry);
    }

    /**
     * DB 작업 타이머
     */
    @Bean
    public Timer databaseOperationTimer(MeterRegistry registry) {
        return Timer.builder("database.operation")
                .description("Database operation timer")
                .tag("operation", "query")
                .register(registry);
    }
}