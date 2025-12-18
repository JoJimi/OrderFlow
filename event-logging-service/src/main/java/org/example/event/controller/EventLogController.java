package org.example.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.dto.response.EventLogResponse;
import org.example.event.service.EventLogService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "EventLog", description = "이벤트 로그 관리 API")
public class EventLogController {

    private final EventLogService eventLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 이벤트 로그 조회", description = "모든 이벤트 로그를 조회합니다 (ADMIN)")
    public ResponseEntity<Page<EventLogResponse>> getAllEventLogs(
            @ParameterObject @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EventLogResponse> events = eventLogService.getAllEventLogs(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문별 이벤트 로그 조회", description = "특정 주문의 이벤트 로그를 조회합니다 (USER)")
    public ResponseEntity<Page<EventLogResponse>> getEventLogsByOrderId(
            @PathVariable String orderId,
            @ParameterObject  @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EventLogResponse> events = eventLogService.getEventLogsByOrderId(orderId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{orderId}/timeline")
    @Operation(summary = "주문 타임라인 조회", description = "특정 주문의 전체 이벤트를 시간순으로 조회합니다 (USER)")
    public ResponseEntity<List<EventLogResponse>> getEventLogsTimeline(
            @PathVariable String orderId
    ) {
        List<EventLogResponse> events = eventLogService.getEventLogsByOrderIdTimeline(orderId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search/by-event-type")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 타입별 조회", description = "특정 타입의 이벤트 로그를 조회합니다 (ADMIN)")
    public ResponseEntity<Page<EventLogResponse>> getEventLogsByEventType(
            @Parameter(description = "이벤트 타입 (예: OrderCreated, PaymentCompleted)")
            @RequestParam String eventType,
            @ParameterObject @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EventLogResponse> events = eventLogService.getEventLogsByEventType(eventType, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search/by-service")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "서비스별 조회", description = "특정 서비스에서 발생한 이벤트 로그를 조회합니다 (ADMIN)")
    public ResponseEntity<Page<EventLogResponse>> getEventLogsByService(
            @Parameter(description = "서비스 이름 (예: order-service, payment-service)")
            @RequestParam String service,
            @ParameterObject @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EventLogResponse> events = eventLogService.getEventLogsByService(service, pageable);
        return ResponseEntity.ok(events);
    }
}