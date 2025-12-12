package org.example.notification.service;

import org.example.notification.domain.Notification;

/**
 * 알림 전송 인터페이스
 * 실제 전송 채널(이메일, SMS, 인앱 푸시 등)은 구현체에서 처리
 */
public interface NotificationSender {

    /**
     * 알림 전송
     */
    void send(Notification notification);

    /**
     * 지원하는 알림 타입 확인
     */
    boolean supports(String channel);
}