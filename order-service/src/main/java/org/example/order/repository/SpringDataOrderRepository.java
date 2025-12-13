package org.example.order.repository;

import org.example.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataOrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.deleted = false ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.deleted = false ORDER BY o.createdAt DESC")
    Page<Order> findAllActiveOrders(Pageable pageable);
}