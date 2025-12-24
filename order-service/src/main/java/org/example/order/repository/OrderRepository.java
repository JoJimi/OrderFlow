package org.example.order.repository;

import org.example.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String orderId);
    Page<Order> findByUserId(String userId, Pageable pageable);
    Page<Order> findAll(Pageable pageable);
    List<String> findPopularProductIds(LocalDateTime since, int limit);
    long countOrdersSince(LocalDateTime since);

}