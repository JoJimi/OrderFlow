package org.example.event.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.event.domain.EventLog;
import org.example.event.repository.EventLogRepository;
import org.example.event.repository.SpringDataEventLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventLogRepositoryAdapter implements EventLogRepository {

    private final SpringDataEventLogRepository springDataEventLogRepository;

    @Override
    public EventLog save(EventLog eventLog) {
        return springDataEventLogRepository.save(eventLog);
    }

    @Override
    public Optional<EventLog> findById(String eventId) {
        return springDataEventLogRepository.findById(eventId);
    }

    @Override
    public Page<EventLog> findAll(Pageable pageable) {
        return springDataEventLogRepository.findAllActiveEventLogs(pageable);
    }

    @Override
    public Page<EventLog> findByOrderId(String orderId, Pageable pageable) {
        return springDataEventLogRepository.findByOrderId(orderId, pageable);
    }

    @Override
    public Page<EventLog> findByEventType(String eventType, Pageable pageable) {
        return springDataEventLogRepository.findByEventType(eventType, pageable);
    }

    @Override
    public Page<EventLog> findByService(String service, Pageable pageable) {
        return springDataEventLogRepository.findByService(service, pageable);
    }

    @Override
    public List<EventLog> findByOrderIdOrderByCreatedAtAsc(String orderId) {
        return springDataEventLogRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}