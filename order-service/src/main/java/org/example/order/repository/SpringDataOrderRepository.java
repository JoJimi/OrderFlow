package org.example.order.repository;

import org.example.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataOrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.deleted = false ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.deleted = false ORDER BY o.createdAt DESC")
    Page<Order> findAllActiveOrders(Pageable pageable);

    @Query(value = """
        SELECT oi.product_id FROM order_items oi
        JOIN orders o ON oi.order_id = o.order_id
        WHERE o.created_at >= :since
          AND o.deleted = false
        GROUP BY oi.product_id
        ORDER BY COUNT(oi.product_id) DESC, SUM(oi.quantity) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findPopularProductIds(
            @Param("since") LocalDateTime since,
            @Param("limit") int limit
    );

    /**
     * 특정 기간 동안의 주문 통계
     */
    @Query("SELECT COUNT(DISTINCT o.orderId) FROM Order o WHERE o.createdAt >= :since AND o.deleted = false")
    long countOrdersSince(@Param("since") LocalDateTime since);
}