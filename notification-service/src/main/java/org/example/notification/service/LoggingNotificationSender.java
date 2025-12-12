package org.example.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.example.notification.domain.Notification;
import org.springframework.stereotype.Component;

/**
 * 로깅 알림 전송자 (개발/테스트용)
 * 실제 프로덕션에서는 이메일/SMS/푸시 알림 구현체로 대체
 */
@Component
@Slf4j
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(Notification notification) {
        log.info("=".repeat(80));
        log.info("📢 알림 전송 시뮬레이션");
        log.info("수신자: {}", notification.getUserId());
        log.info("타입: {}", notification.getNotificationType().getDescription());
        log.info("메시지: {}", notification.getMessage());
        log.info("주문ID: {}", notification.getRelatedOrderId());
        log.info("=".repeat(80));
    }

    @Override
    public boolean supports(String channel) {
        return "LOG".equalsIgnoreCase(channel);
    }
}