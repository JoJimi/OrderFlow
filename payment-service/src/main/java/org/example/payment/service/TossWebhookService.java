package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.domain.Payment;
import org.example.payment.kafka.producer.PaymentEventProducer;
import org.example.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Toss Payments 웹훅 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TossWebhookService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;

    @Transactional
    public void handlePaymentStatusChanged(Map<String, Object> payload) {
        String paymentKey = (String) payload.get("paymentKey");
        String status = (String) payload.get("status");

        log.info("결제 상태 변경 웹훅 - paymentKey: {}, status: {}", paymentKey, status);

        Payment payment = paymentRepository.findByTossPaymentKey(paymentKey)
                .orElse(null);

        if (payment == null) {
            log.warn("웹훅: 결제 정보 없음 - paymentKey: {}", paymentKey);
            return;
        }

        switch (status) {
            case "DONE" -> {
                if (!payment.isCompleted()) {
                    log.info("웹훅: 결제 완료 - paymentKey: {}", paymentKey);
                }
            }
            case "CANCELED" -> {
                if (payment.isCompleted()) {
                    log.info("웹훅: 결제 취소됨 - paymentKey: {}", paymentKey);
                }
            }
            default -> log.info("웹훅: 기타 상태 - status: {}", status);
        }
    }

    @Transactional
    public void handleVirtualAccountDeposit(Map<String, Object> payload) {
        String paymentKey = (String) payload.get("paymentKey");
        String status = (String) payload.get("status");

        log.info("가상계좌 입금 웹훅 - paymentKey: {}, status: {}", paymentKey, status);

        if ("DONE".equals(status)) {
            Payment payment = paymentRepository.findByTossPaymentKey(paymentKey)
                    .orElse(null);

            if (payment != null && payment.isPending()) {
                log.info("가상계좌 입금 완료 - orderId: {}", payment.getOrderId());
            }
        }
    }

    @Transactional
    public void handleCancelStatusChanged(Map<String, Object> payload) {
        String paymentKey = (String) payload.get("paymentKey");
        log.info("취소 상태 변경 웹훅 - paymentKey: {}", paymentKey);
    }
}