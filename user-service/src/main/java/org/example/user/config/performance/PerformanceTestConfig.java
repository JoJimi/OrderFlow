package org.example.user.config.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.sql.DataSource;

/**
 * 성능 테스트용 설정
 * 실제 프로덕션 설정과 분리하여 테스트 환경 구성
 */
@Configuration
@ConditionalOnProperty(name = "performance.test.enabled", havingValue = "true", matchIfMissing = true)
public class PerformanceTestConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * 성능 테스트용 DataSource (테스트 격리)
     */
    @Bean(name = "performanceTestDataSource")
    public DataSource performanceTestDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5); // 테스트용 작은 풀 크기
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setPoolName("PerformanceTestPool");

        return new HikariDataSource(config);
    }

    /**
     * 성능 테스트용 RedisTemplate
     */
    @Bean(name = "performanceTestRedisTemplate")
    public RedisTemplate<String, Object> performanceTestRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}