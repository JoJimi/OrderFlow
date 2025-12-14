package org.example.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.domain.EventLog;
import org.example.event.dto.response.EventLogResponse;
import org.example.event.repository.EventLogRepository;
import org.example.shared.dto.OrderEvent;
import org.example.shared.type.common.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 로그 저장
     */
    @Transactional
    public EventLog saveEventLog(OrderEvent event, String service) {
        log.info("이벤트 로그 저장 - EventId: {}, EventType: {}, Service: {}",
                event.eventId(), event.eventType(), service);

        try {
            // 이벤트 데이터를 JSON 문자열로 변환
            String eventData = objectMapper.writeValueAsString(event);

            EventLog eventLog = EventLog.builder()
                    .eventId(event.eventId())
                    .orderId(event.orderId())
                    .eventType(event.eventType().name())
                    .eventData(eventData)
                    .service(service)
                    .status(EventStatus.SUCCESS)
                    .build();

            EventLog savedLog = eventLogRepository.save(eventLog);
            log.info("이벤트 로그 저장 완료 - EventId: {}", savedLog.getEventId());

            return savedLog;

        } catch (JsonProcessingException e) {
            log.error("이벤트 데이터 JSON 변환 실패 - EventId: {}", event.eventId(), e);

            // JSON 변환 실패 시에도 로그는 저장 (데이터는 null)
            EventLog eventLog = EventLog.builder()
                    .eventId(event.eventId())
                    .orderId(event.orderId())
                    .eventType(event.eventType().name())
                    .eventData(null)
                    .service(service)
                    .status(EventStatus.FAILED)
                    .build();

            return eventLogRepository.save(eventLog);
        }
    }

    /**
     * 전체 이벤트 로그 조회 (관리자)
     */
    @Transactional(readOnly = true)
    public Page<EventLogResponse> getAllEventLogs(Pageable pageable) {
        log.info("전체 이벤트 로그 조회 - 페이지: {}", pageable.getPageNumber());
        return eventLogRepository.findAll(pageable)
                .map(EventLogResponse::from);
    }

    /**
     * 주문별 이벤트 로그 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<EventLogResponse> getEventLogsByOrderId(String orderId, Pageable pageable) {
        log.info("주문별 이벤트 로그 조회 - OrderId: {}, 페이지: {}", orderId, pageable.getPageNumber());
        return eventLogRepository.findByOrderId(orderId, pageable)
                .map(EventLogResponse::from);
    }

    /**
     * 주문별 이벤트 로그 조회 (전체, 시간순)
     */
    @Transactional(readOnly = true)
    public List<EventLogResponse> getEventLogsByOrderIdTimeline(String orderId) {
        log.info("주문 타임라인 조회 - OrderId: {}", orderId);
        return eventLogRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(EventLogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 타입별 조회
     */
    @Transactional(readOnly = true)
    public Page<EventLogResponse> getEventLogsByEventType(String eventType, Pageable pageable) {
        log.info("이벤트 타입별 조회 - EventType: {}, 페이지: {}", eventType, pageable.getPageNumber());
        return eventLogRepository.findByEventType(eventType, pageable)
                .map(EventLogResponse::from);
    }

    /**
     * 서비스별 조회
     */
    @Transactional(readOnly = true)
    public Page<EventLogResponse> getEventLogsByService(String service, Pageable pageable) {
        log.info("서비스별 조회 - Service: {}, 페이지: {}", service, pageable.getPageNumber());
        return eventLogRepository.findByService(service, pageable)
                .map(EventLogResponse::from);
    }
}