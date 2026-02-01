package org.example.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.config.TossPaymentsProperties;
import org.example.payment.dto.response.PaymentResponse;
import org.example.payment.dto.toss.TossPaymentConfirmRequest;
import org.example.payment.service.PaymentService;
import org.example.shared.security.annotation.CurrentUser;
import org.example.shared.security.userdetails.SecurityUser;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "결제 관리 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentsProperties tossProperties;

    /**
     * 결제 위젯 초기화 정보 조회
     * 프론트엔드에서 Toss 결제 위젯을 초기화할 때 필요한 정보 반환
     */
    @GetMapping("/{orderId}/widget")
    @Operation(
            summary = "결제 위젯 정보 조회",
            description = "Toss 결제 위젯 초기화에 필요한 정보를 반환합니다"
    )
    public ResponseEntity<Map<String, Object>> getWidgetInfo(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        log.info("결제 위젯 정보 조회 - orderId: {}, userId: {}", orderId, securityUser.getUserId());

        PaymentResponse payment = paymentService.getPaymentForWidget(orderId, securityUser.getUserId());

        return ResponseEntity.ok(Map.of(
                "clientKey", tossProperties.getClientKey(),
                "orderId", payment.tossOrderId(),
                "orderName", "OrderFlow 주문 결제",
                "amount", payment.amount(),
                "customerName", securityUser.getUserId(),
                "paymentId", payment.paymentId()
        ));
    }

    /**
     * Toss 결제 승인
     * 프론트엔드에서 결제 완료 후 호출
     */
    @PostMapping("/confirm")
    @Operation(
            summary = "결제 승인",
            description = "Toss 결제 위젯에서 결제 완료 후, 서버에서 최종 승인을 요청합니다"
    )
    public ResponseEntity<PaymentResponse> confirmPayment(
            @CurrentUser SecurityUser securityUser,
            @Valid @RequestBody TossPaymentConfirmRequest request
    ) {
        log.info("결제 승인 요청 - orderId: {}, userId: {}",
                request.orderId(), securityUser.getUserId());

        PaymentResponse response = paymentService.confirmPayment(
                securityUser.getUserId(),
                request
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(
            summary = "결제 취소",
            description = "완료된 결제를 취소합니다 (환불 처리)"
    )
    public ResponseEntity<PaymentResponse> cancelPayment(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId,
            @RequestBody Map<String, String> request
    ) {
        log.info("결제 취소 요청 - orderId: {}, userId: {}", orderId, securityUser.getUserId());

        String cancelReason = request.getOrDefault("cancelReason", "사용자 요청에 의한 취소");
        PaymentResponse response = paymentService.cancelPayment(orderId, cancelReason);

        return ResponseEntity.ok(response);
    }

    /**
     * 결제 상태 동기화 (Toss에서 최신 정보 조회)
     */
    @PostMapping("/{orderId}/sync")
    @Operation(
            summary = "결제 상태 동기화",
            description = "Toss Payments에서 최신 결제 상태를 조회하여 동기화합니다"
    )
    public ResponseEntity<PaymentResponse> syncPaymentStatus(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        log.info("결제 상태 동기화 - orderId: {}", orderId);

        PaymentResponse response = paymentService.syncPaymentStatus(orderId);

        return ResponseEntity.ok(response);
    }

    // ===== 기존 API =====

    @GetMapping("/{orderId}")
    @Operation(summary = "주문별 결제 조회")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(
                orderId, securityUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "결제 목록 조회")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @CurrentUser SecurityUser securityUser,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PaymentResponse> payments;
        if ("ROLE_ADMIN".equals(securityUser.getRole())) {
            payments = paymentService.getAllPayments(pageable);
        } else {
            payments = paymentService.getMyPayments(securityUser.getUserId(), pageable);
        }
        return ResponseEntity.ok(payments);
    }
}