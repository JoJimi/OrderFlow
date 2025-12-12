package org.example.notification.dto.response;

/**
 * 안읽은 알림 개수 응답 DTO
 */
public record UnreadCountResponse(
        long unreadCount
) {
}