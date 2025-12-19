package org.example.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.dto.response.InventoryResponse;
import org.example.inventory.service.InventoryService;
import org.example.shared.security.annotation.RequireAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "재고 관리 API")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "재고 조회", description = "특정 상품의 재고 정보를 조회합니다 (ADMIN)")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        log.info("재고 조회 요청 - productId: {}", productId);

        InventoryResponse response = inventoryService.getInventoryByProductId(productId);

        return ResponseEntity.ok(response);
    }
}