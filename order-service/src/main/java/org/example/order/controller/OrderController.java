package org.example.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.dto.request.OrderCreateRequest;
import org.example.order.dto.response.OrderResponse;
import org.example.order.service.OrderService;
import org.example.shared.security.annotation.CurrentUser;
import org.example.shared.security.userdetails.SecurityUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "주문 관리 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다 (USER)")
    public ResponseEntity<OrderResponse> createOrder(
            @CurrentUser SecurityUser securityUser,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderResponse response = orderService.createOrder(securityUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "사용자 본인의 주문 목록을 조회합니다 (USER) / 전체 주문 목록 조회 (ADMIN)")
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @CurrentUser SecurityUser securityUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponse> orders;
        if ("ROLE_ADMIN".equals(securityUser.getRole())) {
            orders = orderService.getAllOrders(pageable);                           // 관리자는 전체 주문 조회
        } else {
            orders = orderService.getMyOrders(securityUser.getUserId(), pageable);  // 일반 사용자는 본인 주문만 조회
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다 (USER)")
    public ResponseEntity<OrderResponse> getOrder(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        OrderResponse response = orderService.getOrder(orderId, securityUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다 (OrderCreated 상태에서만 가능)")
    public ResponseEntity<Void> cancelOrder(
            @CurrentUser SecurityUser securityUser,
            @PathVariable String orderId
    ) {
        orderService.cancelOrder(orderId, securityUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}