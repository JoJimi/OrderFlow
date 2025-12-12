package org.example.notification.repository;

import org.example.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 알림 저장소 인터페이스 (도메인 계층 Port)
 */
public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(String notificationId);
    Page<Notification> findByUserId(String userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsRead(String userId, boolean isRead, Pageable pageable);
    long countUnreadByUserId(String userId);
}