package org.example.notification.dto.response;

import org.example.notification.domain.Notification;
import org.example.shared.type.notification.NotificationType;

import java.time.LocalDateTime;

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