package org.example.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.domain.Notification;
import org.example.notification.dto.response.NotificationResponse;
import org.example.notification.dto.response.UnreadCountResponse;
import org.example.notification.repository.NotificationRepository;
import org.example.shared.exception.notification.InvalidRecipientException;
import org.example.shared.exception.notification.NotificationSendFailedException;
import org.example.shared.type.notification.NotificationType;
import org.example.shared.util.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> notificationSenders;

    /**
     * 알림 생성 및 전송
     */
    @Transactional
    public void createAndSendNotification(
            String userId,
            String message,
            NotificationType notificationType,
            String relatedOrderId
    ) {
        log.info("알림 생성 - userId: {}, type: {}, orderId: {}",
                userId, notificationType, relatedOrderId);

        // 알림 엔티티 생성
        String notificationId = IdGenerator.generateNotificationId();
        Notification notification = Notification.builder()
                .notificationId(notificationId)
                .userId(userId)
                .message(message)
                .notificationType(notificationType)
                .relatedOrderId(relatedOrderId)
                .isRead(false)
                .build();

        // DB 저장
        notificationRepository.save(notification);

        // 비동기로 알림 전송
        sendNotificationAsync(notification);

        log.info("알림 생성 완료 - notificationId: {}", notificationId);
    }

    /**
     * 알림 전송 (비동기)
     */
    @Async
    protected void sendNotificationAsync(Notification notification) {
        for (NotificationSender sender : notificationSenders) {
            try {
                sender.send(notification);
            } catch (Exception e) {
                log.error("알림 전송 실패 - notificationId: {}, sender: {}",
                        notification.getNotificationId(), sender.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 사용자 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(String userId, Pageable pageable) {
        log.info("알림 목록 조회 - userId: {}, page: {}", userId, pageable.getPageNumber());
        return notificationRepository.findByUserId(userId, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 사용자 안읽은 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyUnreadNotifications(String userId, Pageable pageable) {
        log.info("안읽은 알림 목록 조회 - userId: {}, page: {}", userId, pageable.getPageNumber());
        return notificationRepository.findByUserIdAndIsRead(userId, false, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 안읽은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userId) {
        log.info("안읽은 알림 개수 조회 - userId: {}", userId);
        long count = notificationRepository.countUnreadByUserId(userId);
        return new UnreadCountResponse(count);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public NotificationResponse markAsRead(String notificationId, String userId) {
        log.info("알림 읽음 처리 - notificationId: {}, userId: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationSendFailedException::new);

        // 본인 알림인지 확인
        if (!notification.getUserId().equals(userId)) {
            throw new InvalidRecipientException();
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("알림 읽음 처리 완료 - notificationId: {}", notificationId);

        return NotificationResponse.from(notification);
    }

    /**
     * 모든 알림 읽음 처리
     * Bulk Update로 개선
     */
    @Transactional
    public void markAllAsRead(String userId) {
        log.info("모든 알림 읽음 처리 - userId: {}", userId);

        int updatedCount = notificationRepository.markAllAsReadByUserId(userId);

        log.info("모든 알림 읽음 처리 완료 - userId: {}, count: {}", userId, updatedCount);
    }
}