package org.example.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.dto.response.NotificationResponse;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * Redis Pub/Sub 구독자
 * Redis로부터 알림을 받아서 이 서버에 연결된 클라이언트에게 전달
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 채널 이름에서 userId 추출 (notification:userId)
            String channel = new String(message.getChannel());
            String userId = channel.substring("notification:".length());

            // 메시지 파싱
            String messageBody = new String(message.getBody());
            NotificationResponse notification = objectMapper.readValue(
                    messageBody,
                    NotificationResponse.class
            );

            log.info("📥 Redis 알림 수신 - userId: {}, channel: {}", userId, channel);

            // 이 서버에 연결된 클라이언트에게만 전송
            if (sseEmitterService.isConnected(userId)) {
                sseEmitterService.sendToClient(userId, notification);
            } else {
                log.debug("이 서버에 연결되지 않음 - userId: {}", userId);
            }

        } catch (Exception e) {
            log.error("Redis 메시지 처리 중 오류", e);
        }
    }
}