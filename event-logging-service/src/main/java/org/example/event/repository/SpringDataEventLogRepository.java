package org.example.event.repository;

import org.example.event.domain.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA EventLog Repository
 */
public interface SpringDataEventLogRepository extends JpaRepository<EventLog, String> {

    /**
     * 주문별 이벤트 로그 조회 (페이징)
     */
    @Query("SELECT e FROM EventLog e WHERE e.orderId = :orderId AND e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findByOrderId(@Param("orderId") String orderId, Pageable pageable);

    /**
     * 주문별 이벤트 로그 조회 (전체, 시간순)
     */
    @Query("SELECT e FROM EventLog e WHERE e.orderId = :orderId AND e.deleted = false ORDER BY e.createdAt ASC")
    List<EventLog> findByOrderIdOrderByCreatedAtAsc(@Param("orderId") String orderId);

    /**
     * 이벤트 타입별 조회
     */
    @Query("SELECT e FROM EventLog e WHERE e.eventType = :eventType AND e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findByEventType(@Param("eventType") String eventType, Pageable pageable);

    /**
     * 서비스별 조회
     */
    @Query("SELECT e FROM EventLog e WHERE e.service = :service AND e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findByService(@Param("service") String service, Pageable pageable);

    /**
     * 전체 이벤트 로그 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT e FROM EventLog e WHERE e.deleted = false ORDER BY e.createdAt DESC")
    Page<EventLog> findAllActiveEventLogs(Pageable pageable);
}