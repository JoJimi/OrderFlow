package org.example.payment.repository;

import org.example.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<Payment, String> {

    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.deleted = false")
    Optional<Payment> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT p FROM Payment p WHERE p.tossOrderId = :tossOrderId AND p.deleted = false")
    Optional<Payment> findByTossOrderId(@Param("tossOrderId") String tossOrderId);

    @Query("SELECT p FROM Payment p WHERE p.tossPaymentKey = :tossPaymentKey AND p.deleted = false")
    Optional<Payment> findByTossPaymentKey(@Param("tossPaymentKey") String tossPaymentKey);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.deleted = false")
    Page<Payment> findByUserId(@Param("userId") String userId, Pageable pageable);
}