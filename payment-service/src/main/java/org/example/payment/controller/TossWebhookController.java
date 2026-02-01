package org.example.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.service.TossWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Toss Payments 웹훅 수신 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class TossWebhookController {

    private final TossWebhookService webhookService;

    /**
     * Toss 웹훅 수신
     */
    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "TossPayments-Signature", required = false) String signature
    ) {
        log.info("Toss 웹훅 수신 - eventType: {}", payload.get("eventType"));

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