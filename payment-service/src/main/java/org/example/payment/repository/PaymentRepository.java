package org.example.payment.repository;

import org.example.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 결제 저장소 인터페이스 (도메인 계층 Port)
 */
public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(String paymentId);
    Optional<Payment> findByOrderId(String orderId);
    Page<Payment> findByUserId(String userId, Pageable pageable);
    Page<Payment> findAll(Pageable pageable);
}