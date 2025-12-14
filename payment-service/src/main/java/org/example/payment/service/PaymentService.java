package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.domain.Payment;
import org.example.payment.dto.response.PaymentResponse;
import org.example.payment.repository.PaymentRepository;
import org.example.shared.exception.payment.PaymentNotFoundException;
import org.example.shared.type.payment.PaymentMethod;
import org.example.shared.type.payment.PaymentStatus;
import org.example.shared.util.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;
    private final PaymentEventProducer eventProducer;

    /**
     * 결제 처리 (OrderCreated 이벤트 수신 시 호출)
     */
    @Transactional
    public void processPayment(String orderId, String userId, BigDecimal amount) {
        log.info("결제 처리 시작 - orderId: {}, userId: {}, amount: {}", orderId, userId, amount);

        // 1. 결제 ID 생성
        String paymentId = IdGenerator.generatePaymentId();

        // 2. 결제 엔티티 생성 (초기 상태: PAYMENT_PENDING)
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .paymentStatus(PaymentStatus.PAYMENT_PENDING)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // 3. DB 저장
        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 요청 저장 완료 - paymentId: {}, status: PAYMENT_PENDING", paymentId);

        // 4. 외부 결제 API 호출 (시뮬레이션)
        PaymentProcessor.PaymentResult result = paymentProcessor.process(orderId, amount);

        // 5. 결제 결과에 따른 처리
        if (result.success()) {
            handlePaymentSuccess(savedPayment, result.transactionId());
        } else {
            handlePaymentFailure(savedPayment, result.failureReason());
        }
    }

    /**
     * 결제 성공 처리
     */
    private void handlePaymentSuccess(Payment payment, String transactionId) {
        log.info("결제 성공 처리 - paymentId: {}, transactionId: {}",
                payment.getPaymentId(), transactionId);

        payment.complete(transactionId);
        paymentRepository.save(payment);

        eventProducer.publishPaymentCompletedEvent(payment);

        log.info("결제 성공 처리 완료 - paymentId: {}", payment.getPaymentId());
    }

    /**
     * 결제 실패 처리
     */
    private void handlePaymentFailure(Payment payment, String failureReason) {
        log.warn("결제 실패 처리 - paymentId: {}, reason: {}",
                payment.getPaymentId(), failureReason);

        payment.fail(failureReason);
        paymentRepository.save(payment);

        eventProducer.publishPaymentFailedEvent(payment);

        log.info("결제 실패 처리 완료 - paymentId: {}", payment.getPaymentId());
    }

    /**
     * 주문별 결제 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId, String userId) {
        log.info("결제 조회 - orderId: {}, userId: {}", orderId, userId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        // 본인 결제인지 확인
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentNotFoundException();
        }

        return PaymentResponse.from(payment);
    }

    /**
     * 사용자별 결제 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(String userId, Pageable pageable) {
        log.info("사용자 결제 목록 조회 - userId: {}, page: {}", userId, pageable.getPageNumber());
        return paymentRepository.findByUserId(userId, pageable)
                .map(PaymentResponse::from);
    }

    /**
     * 전체 결제 목록 조회 (관리자)
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        log.info("전체 결제 목록 조회 - page: {}", pageable.getPageNumber());
        return paymentRepository.findAll(pageable)
                .map(PaymentResponse::from);
    }
}