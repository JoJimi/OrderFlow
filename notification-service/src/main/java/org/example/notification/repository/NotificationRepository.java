package org.example.notification.repository;

import org.example.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(String notificationId);
    Page<Notification> findByUserId(String userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsRead(String userId, boolean isRead, Pageable pageable);
    long countUnreadByUserId(String userId);
}