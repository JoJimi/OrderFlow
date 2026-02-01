package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.client.TossPaymentsClient;
import org.example.payment.domain.Payment;
import org.example.payment.dto.response.PaymentResponse;
import org.example.payment.dto.toss.TossPaymentCancelRequest;
import org.example.payment.dto.toss.TossPaymentConfirmRequest;
import org.example.payment.dto.toss.TossPaymentResponse;
import org.example.payment.exception.TossPaymentException;
import org.example.payment.kafka.producer.PaymentEventProducer;
import org.example.payment.repository.PaymentRepository;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;
import org.example.shared.exception.payment.PaymentAlreadyCompletedException;
import org.example.shared.exception.payment.PaymentNotFoundException;
import org.example.shared.type.payment.PaymentMethod;
import org.example.shared.type.payment.PaymentStatus;
import org.example.shared.util.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentEventProducer eventProducer;

    /**
     * 결제 레코드 생성 (OrderCreated 이벤트 수신 시 호출)
     */
    @Transactional
    public PaymentResponse createPaymentRecord(String orderId, String userId, BigDecimal amount) {
        log.info("결제 레코드 생성 - orderId: {}, userId: {}, amount: {}", orderId, userId, amount);

        // 중복 생성 방지
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.warn("이미 존재하는 결제 레코드 - orderId: {}", orderId);
            return PaymentResponse.from(paymentRepository.findByOrderId(orderId).get());
        }

        String paymentId = IdGenerator.generatePaymentId();
        String tossOrderId = generateTossOrderId(orderId);
        String idempotencyKey = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .paymentStatus(PaymentStatus.PAYMENT_PENDING)
                .tossOrderId(tossOrderId)
                .idempotencyKey(idempotencyKey)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("결제 레코드 생성 완료 - paymentId: {}, tossOrderId: {}", paymentId, tossOrderId);

        return PaymentResponse.from(saved);
    }

    /**
     * Toss 결제 승인 (프론트엔드에서 결제 완료 후 호출)
     */
    @Transactional
    public PaymentResponse confirmPayment(String userId, TossPaymentConfirmRequest request) {
        log.info("Toss 결제 승인 시작 - orderId: {}, paymentKey: {}, amount: {}",
                request.orderId(), request.paymentKey(), request.amount());

        // 1. 결제 정보 조회 (tossOrderId로 조회)
        Payment payment = paymentRepository.findByTossOrderId(request.orderId())
                .orElseThrow(() -> {
                    log.error("결제 정보 없음 - tossOrderId: {}", request.orderId());
                    return new PaymentNotFoundException();
                });

        // 2. 본인 확인
        if (!payment.getUserId().equals(userId)) {
            log.warn("결제 권한 없음 - orderId: {}, requestUserId: {}, paymentUserId: {}",
                    request.orderId(), userId, payment.getUserId());
            throw new PaymentNotFoundException();
        }

        // 3. 상태 확인
        if (!payment.isPending()) {
            log.warn("결제 대기 상태가 아님 - orderId: {}, status: {}",
                    request.orderId(), payment.getPaymentStatus());
            throw new PaymentAlreadyCompletedException();
        }

        // 4. 금액 검증 (클라이언트 조작 방지)
        BigDecimal requestAmount = BigDecimal.valueOf(request.amount());
        if (payment.getAmount().compareTo(requestAmount) != 0) {
            log.error("결제 금액 불일치! 서버: {}, 요청: {} - 조작 시도 의심",
                    payment.getAmount(), requestAmount);
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_MISMATCH,
                    "결제 금액이 일치하지 않습니다.");
        }

        // 5. Toss Payments API 호출
        try {
            TossPaymentResponse tossResponse = tossPaymentsClient.confirmPayment(
                    request,
                    payment.getIdempotencyKey()
            );

            // 6. 결제 성공 처리
            if (tossResponse.isSuccess()) {
                handlePaymentSuccess(payment, tossResponse);
            } else if (tossResponse.isWaitingForDeposit()) {
                log.info("가상계좌 입금 대기 - orderId: {}", request.orderId());
            } else {
                log.warn("예상치 못한 결제 상태 - status: {}", tossResponse.status());
            }

            return PaymentResponse.from(payment);

        } catch (TossPaymentException e) {
            handlePaymentFailure(payment, e.getTossErrorCode(), e.getUserFriendlyMessage());
            throw e;
        }
    }

    /**
     * 결제 성공 처리
     */
    private void handlePaymentSuccess(Payment payment, TossPaymentResponse response) {
        log.info("결제 성공 처리 - paymentKey: {}, approvedAt: {}",
                response.paymentKey(), response.approvedAt());

        String cardCompany = null;
        String cardNumber = null;
        Integer installmentMonths = null;

        if (response.card() != null) {
            cardCompany = response.card().issuerCode();
            cardNumber = response.card().number();
            installmentMonths = response.card().installmentPlanMonths();
        }

        PaymentMethod method = determinePaymentMethod(response.method());
        String receiptUrl = response.receipt() != null ? response.receipt().url() : null;

        payment.completeWithToss(
                response.paymentKey(),
                response.lastTransactionKey(),
                response.approvedAt(),
                cardCompany,
                cardNumber,
                installmentMonths,
                receiptUrl,
                method
        );

        paymentRepository.save(payment);
        eventProducer.publishPaymentCompletedEvent(payment);

        log.info("결제 성공 처리 완료 - paymentId: {}, orderId: {}",
                payment.getPaymentId(), payment.getOrderId());
    }

    /**
     * 결제 실패 처리
     */
    private void handlePaymentFailure(Payment payment, String failureCode, String failureReason) {
        log.warn("결제 실패 처리 - paymentId: {}, code: {}, reason: {}",
                payment.getPaymentId(), failureCode, failureReason);

        payment.fail(failureCode, failureReason);
        paymentRepository.save(payment);
        eventProducer.publishPaymentFailedEvent(payment);

        log.info("결제 실패 처리 완료 - paymentId: {}", payment.getPaymentId());
    }

    /**
     * 결제 취소 (이벤트 기반 - 기본 취소 사유 사용)
     */
    @Transactional
    public void cancelPayment(String orderId) {
        cancelPayment(orderId, "주문 취소에 의한 결제 취소");
    }

    /**
     * 결제 취소 (관리자 또는 주문 취소 시)
     */
    @Transactional
    public PaymentResponse cancelPayment(String orderId, String cancelReason) {
        log.info("결제 취소 요청 - orderId: {}, reason: {}", orderId, cancelReason);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        // PENDING 상태면 Toss API 호출 없이 취소
        if (payment.isPending()) {
            payment.fail("CANCELLED", cancelReason);
            paymentRepository.save(payment);
            eventProducer.publishPaymentCancelledEvent(payment);
            return PaymentResponse.from(payment);
        }

        // 완료된 결제는 Toss API로 취소
        if (payment.isCompleted() && payment.getTossPaymentKey() != null) {
            try {
                String cancelIdempotencyKey = UUID.randomUUID().toString();
                TossPaymentResponse response = tossPaymentsClient.cancelPayment(
                        payment.getTossPaymentKey(),
                        TossPaymentCancelRequest.fullCancel(cancelReason),
                        cancelIdempotencyKey
                );

                if (response.isCanceled()) {
                    payment.fail("CANCELLED", cancelReason);
                    paymentRepository.save(payment);
                    eventProducer.publishPaymentCancelledEvent(payment);
                }

                return PaymentResponse.from(payment);

            } catch (TossPaymentException e) {
                log.error("Toss 결제 취소 실패 - orderId: {}, error: {}",
                        orderId, e.getMessage());
                throw e;
            }
        }

        log.warn("취소할 수 없는 결제 상태 - orderId: {}, status: {}",
                orderId, payment.getPaymentStatus());
        return PaymentResponse.from(payment);
    }

    /**
     * 결제 정보 조회 (Toss에서 최신 정보 동기화)
     */
    @Transactional
    public PaymentResponse syncPaymentStatus(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        if (payment.getTossPaymentKey() == null) {
            return PaymentResponse.from(payment);
        }

        try {
            TossPaymentResponse response = tossPaymentsClient.getPayment(payment.getTossPaymentKey());

            if (response.isSuccess() && !payment.isCompleted()) {
                handlePaymentSuccess(payment, response);
            } else if (response.isCanceled() && payment.isCompleted()) {
                payment.fail("CANCELLED", "Toss에서 취소됨");
                paymentRepository.save(payment);
            }

            return PaymentResponse.from(payment);

        } catch (TossPaymentException e) {
            log.warn("결제 상태 동기화 실패 - orderId: {}", orderId);
            return PaymentResponse.from(payment);
        }
    }

    /**
     * 프론트엔드 결제 위젯 초기화용 정보 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentForWidget(String orderId, String userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        if (!payment.getUserId().equals(userId)) {
            throw new PaymentNotFoundException();
        }

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId, String userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentNotFoundException::new);

        if (!payment.getUserId().equals(userId)) {
            throw new PaymentNotFoundException();
        }

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(String userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(PaymentResponse::from);
    }

    private String generateTossOrderId(String orderId) {
        String sanitized = orderId.replace("-", "");
        return "ORD_" + sanitized + "_" + System.currentTimeMillis();
    }

    private PaymentMethod determinePaymentMethod(String tossMethod) {
        if (tossMethod == null) return PaymentMethod.CREDIT_CARD;

        return switch (tossMethod) {
            case "카드" -> PaymentMethod.CREDIT_CARD;
            case "가상계좌" -> PaymentMethod.BANK_TRANSFER;
            case "간편결제" -> PaymentMethod.MOBILE_PAYMENT;
            case "계좌이체" -> PaymentMethod.BANK_TRANSFER;
            case "휴대폰" -> PaymentMethod.MOBILE_PAYMENT;
            default -> PaymentMethod.CREDIT_CARD;
        };
    }
}