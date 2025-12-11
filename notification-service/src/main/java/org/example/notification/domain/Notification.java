package org.example.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * 사용자에게 전송되는 알림 정보
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_order_id", columnList = "related_order_id"),
        @Index(name = "idx_notification_type", columnList = "notification_type"),
        @Index(name = "idx_is_read", columnList = "is_read"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Notification extends BaseEntity {

    @Id
    @Column(name = "notification_id", nullable = false, length = 50)
    private String notificationId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "related_order_id", length = 50)
    private String relatedOrderId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 알림을 읽음 처리
     */
    public void markAsRead() {
        if (this.isRead) {
            return; // 이미 읽음 처리됨
        }
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 알림을 안읽음 처리
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
}