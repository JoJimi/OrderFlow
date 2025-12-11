package org.example.order.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.order.domain.Order;
import org.example.order.repository.OrderRepository;
import org.example.order.repository.SpringDataOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OrderRepository Port-Adapter 구현체
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public Order save(Order order) {
        return springDataOrderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return springDataOrderRepository.findById(orderId);
    }

    @Override
    public Page<Order> findByUserId(String userId, Pageable pageable) {
        return springDataOrderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return springDataOrderRepository.findAllActiveOrders(pageable);
    }
}