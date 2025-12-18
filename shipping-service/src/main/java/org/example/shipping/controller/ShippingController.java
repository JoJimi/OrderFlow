package org.example.shipping.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shipping.dto.request.ShippingStartRequest;
import org.example.shipping.dto.response.ShipmentResponse;
import org.example.shipping.service.ShippingService;
import org.example.shared.security.annotation.RequireAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipping", description = "배송 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/{shipmentId}")
    @Operation(summary = "배송 조회", description = "배송 정보를 조회합니다 (USER)")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable String shipmentId) {
        log.info("배송 조회 요청 - shipmentId: {}", shipmentId);

        ShipmentResponse response = shippingService.getShipment(shipmentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "주문별 배송 조회", description = "주문 ID로 배송 정보를 조회합니다 (USER)")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderId(@PathVariable String orderId) {
        log.info("주문별 배송 조회 요청 - orderId: {}", orderId);

        ShipmentResponse response = shippingService.getShipmentByOrderId(orderId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{shipmentId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "배송 시작", description = "배송을 시작합니다 (ADMIN)")
    public ResponseEntity<ShipmentResponse> startShipping(
            @PathVariable String shipmentId,
            @Valid @RequestBody ShippingStartRequest request
    ) {
        log.info("배송 시작 요청 - shipmentId: {}, trackingNumber: {}",
                shipmentId, request.trackingNumber());

        ShipmentResponse response = shippingService.startShipping(shipmentId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{shipmentId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "배송 완료", description = "배송을 완료 처리합니다 (ADMIN)")
    public ResponseEntity<ShipmentResponse> completeShipping(@PathVariable String shipmentId) {
        log.info("배송 완료 요청 - shipmentId: {}", shipmentId);

        ShipmentResponse response = shippingService.completeShipping(shipmentId);

        return ResponseEntity.ok(response);
    }
}