package org.example.notification.service;

import org.example.notification.domain.Notification;

/**
 * 알림 전송 인터페이스
 * 실제 전송 채널(이메일, SMS, 인앱 푸시 등)은 구현체에서 처리
 */
public interface NotificationSender {

    void send(Notification notification);
    boolean supports(String channel);
}