package org.example.order.repository;

import org.example.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA Order Repository
 */
public interface SpringDataOrderRepository extends JpaRepository<Order, String> {

    /**
     * 사용자별 주문 목록 조회
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.deleted = false ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 전체 주문 목록 조회 (삭제되지 않은 주문만)
     */
    @Query("SELECT o FROM Order o WHERE o.deleted = false ORDER BY o.createdAt DESC")
    Page<Order> findAllActiveOrders(Pageable pageable);
}