package org.example.event.dto.response;

import org.example.event.domain.EventLog;
import org.example.shared.type.EventStatus;

import java.time.LocalDateTime;

/**
 * 이벤트 로그 응답 DTO
 */
public record EventLogResponse(
        String eventId,
        String orderId,
        String eventType,
        String eventData,
        String service,
        EventStatus status,
        LocalDateTime createdAt
) {
    /**
     * EventLog 엔티티로부터 EventLogResponse 생성
     */
    public static EventLogResponse from(EventLog eventLog) {
        return new EventLogResponse(
                eventLog.getEventId(),
                eventLog.getOrderId(),
                eventLog.getEventType(),
                eventLog.getEventData(),
                eventLog.getService(),
                eventLog.getStatus(),
                eventLog.getCreatedAt()
        );
    }
}