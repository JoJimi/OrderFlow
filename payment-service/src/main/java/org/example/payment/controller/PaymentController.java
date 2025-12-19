package org.example.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.dto.response.PaymentResponse;
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

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "결제 관리 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/start")
    @Operation(
            summary = "결제 시작",
            description = "PAYMENT_PENDING 상태의 결제를 실제로 실행합니다 (80% 성공 / 20% 실패)"
    )
    public ResponseEntity<PaymentResponse> startPayment(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        log.info("결제 시작 API 호출 - orderId: {}, userId: {}", orderId, securityUser.getUserId());

        PaymentResponse response = paymentService.startPayment(orderId, securityUser.getUserId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문별 결제 조회", description = "특정 주문의 결제 정보를 조회합니다 (USER)")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, securityUser.getUserId());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "결제 목록 조회", description = "사용자 본인의 결제 목록을 조회합니다 (USER) / 전체 결제 목록 조회 (ADMIN)")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @CurrentUser SecurityUser securityUser,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PaymentResponse> payments;
        if ("ROLE_ADMIN".equals(securityUser.getRole())) {
            // 관리자는 전체 결제 조회
            payments = paymentService.getAllPayments(pageable);
        } else {
            payments = paymentService.getMyPayments(securityUser.getUserId(), pageable);
        }

        return ResponseEntity.ok(payments);
    }
}