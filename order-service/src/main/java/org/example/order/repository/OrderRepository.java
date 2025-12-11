package org.example.order.repository;

import org.example.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Order 저장소 인터페이스 (도메인 계층 Port)
 */
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String orderId);
    Page<Order> findByUserId(String userId, Pageable pageable);
    Page<Order> findAll(Pageable pageable);
}