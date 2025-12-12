package org.example.notification.dto.response;

import org.example.notification.domain.Notification;
import org.example.shared.type.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
public record NotificationResponse(
        String notificationId,
        String userId,
        String message,
        NotificationType notificationType,
        Boolean isRead,
        String relatedOrderId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.getIsRead(),
                notification.getRelatedOrderId(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}