package org.example.event.dto.response;

import org.example.event.domain.EventLog;
import org.example.shared.type.EventStatus;

import java.time.LocalDateTime;

public record EventLogResponse(
        String eventId,
        String orderId,
        String eventType,
        String eventData,
        String service,
        EventStatus status,
        LocalDateTime createdAt
) {
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