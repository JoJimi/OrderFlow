package org.example.event.repository;

import org.example.event.domain.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * EventLog 저장소 인터페이스 (도메인 계층 Port)
 */
public interface EventLogRepository {
    EventLog save(EventLog eventLog);
    Optional<EventLog> findById(String eventId);
    Page<EventLog> findAll(Pageable pageable);
    Page<EventLog> findByOrderId(String orderId, Pageable pageable);
    Page<EventLog> findByEventType(String eventType, Pageable pageable);
    Page<EventLog> findByService(String service, Pageable pageable);
    List<EventLog> findByOrderIdOrderByCreatedAtAsc(String orderId);
}