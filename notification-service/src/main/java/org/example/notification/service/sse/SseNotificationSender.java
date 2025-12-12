package org.example.notification.service.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.domain.Notification;
import org.example.notification.dto.response.NotificationResponse;
import org.example.notification.service.NotificationSender;
import org.example.notification.service.redis.RedisNotificationPublisher;
import org.springframework.stereotype.Component;

/**
 * SSE 알림 전송자
 * Redis Pub/Sub을 통해 모든 서버 인스턴스에 알림 전파
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SseNotificationSender implements NotificationSender {

    private final RedisNotificationPublisher redisPublisher;

    @Override
    public void send(Notification notification) {
        log.info("SSE 알림 전송 시작 - userId: {}, type: {}",
                notification.getUserId(),
                notification.getNotificationType());

        // Redis Pub/Sub을 통해 모든 서버에 전파
        NotificationResponse response = NotificationResponse.from(notification);
        redisPublisher.publish(notification.getUserId(), response);
    }

    @Override
    public boolean supports(String channel) {
        return "SSE".equalsIgnoreCase(channel);
    }
}