package org.example.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * 결제 처리 시뮬레이터
 * - 실제 환경에서는 외부 PG사 API 호출
 * - 개발 환경에서는 랜덤 성공/실패 시뮬레이션
 */
@Component
@Slf4j
public class PaymentProcessor {

    private final Random random = new Random();

    /**
     * 결제 처리 시뮬레이션
     *
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return PaymentResult (성공/실패, 트랜잭션 ID, 실패 사유)
     */
    public PaymentResult process(String orderId, BigDecimal amount) {
        log.info("결제 처리 시작 - orderId: {}, amount: {}", orderId, amount);

        try {
            // 외부 PG사 API 호출 시뮬레이션 (1초 대기)
            Thread.sleep(1000);

            // 80% 성공, 20% 실패
            boolean isSuccess = random.nextInt(100) < 80;

            if (isSuccess) {
                String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                log.info("결제 성공 - orderId: {}, transactionId: {}", orderId, transactionId);
                return PaymentResult.success(transactionId);
            } else {
                String failureReason = getRandomFailureReason();
                log.warn("결제 실패 - orderId: {}, reason: {}", orderId, failureReason);
                return PaymentResult.failure(failureReason);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("결제 처리 중 예외 발생 - orderId: {}", orderId, e);
            return PaymentResult.failure("결제 처리 중 시스템 오류 발생");
        }
    }

    /**
     * 랜덤 실패 사유 생성
     */
    private String getRandomFailureReason() {
        String[] reasons = {
                "카드 한도 초과",
                "카드 정보 불일치",
                "잔액 부족",
                "네트워크 타임아웃",
                "은행 시스템 점검 중"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    /**
     * 결제 처리 결과
     */
    public record PaymentResult(
            boolean success,
            String transactionId,
            String failureReason
    ) {
        public static PaymentResult success(String transactionId) {
            return new PaymentResult(true, transactionId, null);
        }

        public static PaymentResult failure(String failureReason) {
            return new PaymentResult(false, null, failureReason);
        }
    }
}