package org.example.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.service.TossWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Toss Payments 웹훅 수신 컨트롤러
 * - 가상계좌 입금 완료
 * - 결제 취소 알림
 * - 정산 완료 알림 등
 */
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class TossWebhookController {

    private final TossWebhookService webhookService;

    /**
     * Toss 웹훅 수신
     * Toss 개발자 센터에서 웹훅 URL 등록 필요
     */
    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "TossPayments-Signature", required = false) String signature
    ) {
        log.info("Toss 웹훅 수신 - eventType: {}", payload.get("eventType"));

        // 웹훅 시그니처 검증 (선택사항이지만 권장)
        // TODO: 시그니처 검증 로직 추가

        String eventType = (String) payload.get("eventType");

        switch (eventType) {
            case "PAYMENT_STATUS_CHANGED" -> webhookService.handlePaymentStatusChanged(payload);
            case "VIRTUAL_ACCOUNT_CALLBACK" -> webhookService.handleVirtualAccountDeposit(payload);
            case "CANCEL_STATUS_CHANGED" -> webhookService.handleCancelStatusChanged(payload);
            default -> log.warn("알 수 없는 웹훅 이벤트 타입: {}", eventType);
        }

        return ResponseEntity.ok().build();
    }
}