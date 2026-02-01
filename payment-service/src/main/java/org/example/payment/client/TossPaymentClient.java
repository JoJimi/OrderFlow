package org.example.payment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.dto.toss.TossErrorResponse;
import org.example.payment.dto.toss.TossPaymentCancelRequest;
import org.example.payment.dto.toss.TossPaymentConfirmRequest;
import org.example.payment.dto.toss.TossPaymentResponse;
import org.example.payment.exception.TossPaymentException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Toss Payments API 클라이언트
 * - Circuit Breaker: 연속 실패 시 빠른 실패 처리
 * - Retry: 일시적 오류 시 재시도
 * - Idempotency Key: 중복 결제 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private static final String CONFIRM_URL = "/v1/payments/confirm";
    private static final String PAYMENT_URL = "/v1/payments/{paymentKey}";
    private static final String CANCEL_URL = "/v1/payments/{paymentKey}/cancel";

    private final WebClient tossPaymentsWebClient;

    /**
     * 결제 승인 요청
     *
     * @param request 프론트엔드에서 전달받은 결제 정보
     * @param idempotencyKey 멱등성 키 (중복 요청 방지)
     * @return 결제 승인 결과
     */
    @CircuitBreaker(name = "tossPayments", fallbackMethod = "confirmPaymentFallback")
    @Retry(name = "tossPayments")
    public TossPaymentResponse confirmPayment(TossPaymentConfirmRequest request, String idempotencyKey) {
        log.info("Toss 결제 승인 요청 - orderId: {}, amount: {}, idempotencyKey: {}",
                request.orderId(), request.amount(), idempotencyKey);

        Map<String, Object> body = Map.of(
                "paymentKey", request.paymentKey(),
                "orderId", request.orderId(),
                "amount", request.amount()
        );

        return tossPaymentsWebClient.post()
                .uri(CONFIRM_URL)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(TossErrorResponse.class)
                                .flatMap(error -> {
                                    log.error("Toss 결제 승인 실패 - code: {}, message: {}",
                                            error.code(), error.message());
                                    return Mono.error(new TossPaymentException(error));
                                }))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }

    /**
     * 결제 승인 실패 시 Fallback
     */
    public TossPaymentResponse confirmPaymentFallback(TossPaymentConfirmRequest request,
                                                      String idempotencyKey,
                                                      Throwable throwable) {
        log.error("Toss 결제 승인 Fallback 실행 - orderId: {}, error: {}",
                request.orderId(), throwable.getMessage());

        throw new TossPaymentException("CIRCUIT_BREAKER_OPEN",
                "결제 시스템이 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");
    }

    /**
     * 결제 조회
     *
     * @param paymentKey 결제 키
     * @return 결제 정보
     */
    @CircuitBreaker(name = "tossPayments", fallbackMethod = "getPaymentFallback")
    @Retry(name = "tossPayments")
    public TossPaymentResponse getPayment(String paymentKey) {
        log.info("Toss 결제 조회 - paymentKey: {}", paymentKey);

        return tossPaymentsWebClient.get()
                .uri(PAYMENT_URL, paymentKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(TossErrorResponse.class)
                                .flatMap(error -> Mono.error(new TossPaymentException(error))))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }

    /**
     * 결제 조회 실패 시 Fallback
     */
    public TossPaymentResponse getPaymentFallback(String paymentKey, Throwable throwable) {
        log.error("Toss 결제 조회 Fallback 실행 - paymentKey: {}, error: {}",
                paymentKey, throwable.getMessage());

        throw new TossPaymentException("CIRCUIT_BREAKER_OPEN",
                "결제 정보를 조회할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    /**
     * 결제 취소
     *
     * @param paymentKey 결제 키
     * @param request 취소 요청 정보
     * @param idempotencyKey 멱등성 키
     * @return 취소 결과
     */
    @CircuitBreaker(name = "tossPayments", fallbackMethod = "cancelPaymentFallback")
    @Retry(name = "tossPayments")
    public TossPaymentResponse cancelPayment(String paymentKey,
                                             TossPaymentCancelRequest request,
                                             String idempotencyKey) {
        log.info("Toss 결제 취소 요청 - paymentKey: {}, reason: {}, amount: {}",
                paymentKey, request.cancelReason(), request.cancelAmount());

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("cancelReason", request.cancelReason());
        if (request.cancelAmount() != null) {
            body.put("cancelAmount", request.cancelAmount());
        }

        return tossPaymentsWebClient.post()
                .uri(CANCEL_URL, paymentKey)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(TossErrorResponse.class)
                                .flatMap(error -> {
                                    log.error("Toss 결제 취소 실패 - code: {}, message: {}",
                                            error.code(), error.message());
                                    return Mono.error(new TossPaymentException(error));
                                }))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }

    /**
     * 결제 취소 실패 시 Fallback
     */
    public TossPaymentResponse cancelPaymentFallback(String paymentKey,
                                                     TossPaymentCancelRequest request,
                                                     String idempotencyKey,
                                                     Throwable throwable) {
        log.error("Toss 결제 취소 Fallback 실행 - paymentKey: {}, error: {}",
                paymentKey, throwable.getMessage());

        throw new TossPaymentException("CIRCUIT_BREAKER_OPEN",
                "결제 취소를 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }
}