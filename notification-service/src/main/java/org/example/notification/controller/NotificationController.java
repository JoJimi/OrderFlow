package org.example.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.dto.response.NotificationResponse;
import org.example.notification.dto.response.UnreadCountResponse;
import org.example.notification.service.NotificationService;
import org.example.notification.service.sse.SseEmitterService;
import org.example.shared.security.annotation.CurrentUser;
import org.example.shared.security.userdetails.SecurityUser;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "알림 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자 본인의 알림 목록을 조회합니다 (USER)")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @CurrentUser SecurityUser securityUser,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("알림 목록 조회 요청 - userId: {}", securityUser.getUserId());

        Page<NotificationResponse> notifications = notificationService
                .getMyNotifications(securityUser.getUserId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @Operation(summary = "안읽은 알림 목록 조회", description = "사용자 본인의 안읽은 알림만 조회합니다 (USER)")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @CurrentUser SecurityUser securityUser,
            @ParameterObject  @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("안읽은 알림 목록 조회 요청 - userId: {}", securityUser.getUserId());

        Page<NotificationResponse> notifications = notificationService
                .getMyUnreadNotifications(securityUser.getUserId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "안읽은 알림 개수 조회", description = "사용자 본인의 안읽은 알림 개수를 조회합니다 (USER)")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @CurrentUser SecurityUser securityUser
    ) {
        log.info("안읽은 알림 개수 조회 요청 - userId: {}", securityUser.getUserId());

        UnreadCountResponse response = notificationService
                .getUnreadCount(securityUser.getUserId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다 (USER)")
    public ResponseEntity<NotificationResponse> markAsRead(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String notificationId
    ) {
        log.info("알림 읽음 처리 요청 - notificationId: {}, userId: {}",
                notificationId, securityUser.getUserId());

        NotificationResponse response = notificationService
                .markAsRead(notificationId, securityUser.getUserId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다 (USER)")
    public ResponseEntity<Void> markAllAsRead(
            @CurrentUser SecurityUser securityUser
    ) {
        log.info("모든 알림 읽음 처리 요청 - userId: {}", securityUser.getUserId());

        notificationService.markAllAsRead(securityUser.getUserId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "실시간 알림 구독", description = "SSE를 통해 실시간 알림을 받습니다 (USER)")
    public SseEmitter subscribe(@CurrentUser SecurityUser securityUser) {
        log.info("SSE 구독 요청 - userId: {}", securityUser.getUserId());
        return sseEmitterService.createEmitter(securityUser.getUserId());
    }

    @GetMapping("/connection-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "SSE 연결 상태", description = "현재 서버의 SSE 연결 수를 반환합니다 (ADMIN)")
    public ResponseEntity<Map<String, Object>> getConnectionStatus() {
        Map<String, Object> status = Map.of(
                "connectionCount", sseEmitterService.getConnectionCount(),
                "serverInstance", System.getenv("HOSTNAME") != null
                        ? System.getenv("HOSTNAME")
                        : "localhost"
        );
        return ResponseEntity.ok(status);
    }
}