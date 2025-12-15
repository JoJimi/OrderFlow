package org.example.notification.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.notification.domain.Notification;
import org.example.notification.repository.NotificationRepository;
import org.example.notification.repository.SpringDataNotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(String notificationId) {
        return jpaRepository.findById(notificationId);
    }

    @Override
    public Page<Notification> findByUserId(String userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Notification> findByUserIdAndIsRead(String userId, boolean isRead, Pageable pageable) {
        return jpaRepository.findByUserIdAndIsRead(userId, isRead, pageable);
    }

    @Override
    public long countUnreadByUserId(String userId) {
        return jpaRepository.countUnreadByUserId(userId);
    }

    @Override
    public int markAllAsReadByUserId(String userId) {
        return jpaRepository.markAllAsReadByUserId(userId);
    }
}