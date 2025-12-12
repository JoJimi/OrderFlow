package org.example.notification.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.dto.response.NotificationResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Pub/Sub 발행자
 * 모든 서버 인스턴스에게 알림 전파
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 특정 사용자에게 알림 발행
     * Redis를 통해 모든 서버 인스턴스에 전파
     */
    public void publish(String userId, NotificationResponse notification) {
        String channel = "notification:" + userId;

        try {
            String message = objectMapper.writeValueAsString(notification);
            redisTemplate.convertAndSend(channel, message);
            log.info("📤 Redis 알림 발행 - userId: {}, channel: {}", userId, channel);
        } catch (JsonProcessingException e) {
            log.error("Redis 알림 발행 실패 - JSON 직렬화 오류", e);
        }
    }
}