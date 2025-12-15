package org.example.event.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.common.EventStatus;

@Entity
@Table(name = "event_logs", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_service", columnList = "service"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EventLog extends BaseEntity {

    @Id
    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;

    @Column(name = "service", nullable = false, length = 50)
    private String service;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.SUCCESS;

    public void updateStatus(EventStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isSuccess() {
        return this.status == EventStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == EventStatus.FAILED;
    }
}
