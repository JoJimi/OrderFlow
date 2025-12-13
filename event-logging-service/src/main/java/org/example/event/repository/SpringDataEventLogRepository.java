package org.example.event.repository;

import org.example.event.domain.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataEventLogRepository extends JpaRepository<EventLog, String> {

    @Query("SELECT e FROM EventLog e WHERE e.orderId = :orderId AND e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findByOrderId(@Param("orderId") String orderId, Pageable pageable);

    @Query("SELECT e FROM EventLog e WHERE e.orderId = :orderId AND e.deleted = false ORDER BY e.createdAt ASC")
    List<EventLog> findByOrderIdOrderByCreatedAtAsc(@Param("orderId") String orderId);

    @Query("SELECT e FROM EventLog e WHERE e.eventType = :eventType AND e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findByEventType(@Param("eventType") String eventType, Pageable pageable);

    @Query("SELECT e FROM EventLog e WHERE e.service = :service AND e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findByService(@Param("service") String service, Pageable pageable);

    @Query("SELECT e FROM EventLog e WHERE e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findAllActiveEventLogs(Pageable pageable);
}