package org.example.payment.repository;

import org.example.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(String paymentId);
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByTossOrderId(String tossOrderId);
    Optional<Payment> findByTossPaymentKey(String tossPaymentKey);
    Page<Payment> findByUserId(String userId, Pageable pageable);
    Page<Payment> findAll(Pageable pageable);
}