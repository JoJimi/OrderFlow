package org.example.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.dto.request.ProductBulkInitRequest;
import org.example.product.dto.request.ProductCreateRequest;
import org.example.product.dto.request.ProductUpdateRequest;
import org.example.product.dto.response.ProductBulkInitResponse;
import org.example.product.dto.response.ProductResponse;
import org.example.product.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product API", description = "상품 관리 API")
public class ProductController {

    private final ProductService productService;

    @Value("${test.delay.enabled:false}")
    private boolean delayEnabled;


    @GetMapping
    @Operation(summary = "전체 상품 목록 조회", description = "모든 상품을 페이지네이션하여 조회합니다")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 필드", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향 (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리의 상품을 조회합니다")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @Parameter(description = "카테고리 코드", example = "ELECTRONICS")
            @PathVariable String category,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", example = "SEED-1-000001")
            @PathVariable String productId
    ) {
/**
*         테스트용 지연 주입
*         if (delayEnabled) {
*             try {
*                 log.warn("⏱️ 테스트용 2초 지연 시작 - productId: {}", productId);
*                 Thread.sleep(2000);
*                 log.warn("⏱️ 테스트용 2초 지연 종료 - productId: {}", productId);
*             } catch (InterruptedException e) {
*                 Thread.currentThread().interrupt();
*             }
*         }
*/

        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/search")
    @Operation(summary = "상품 검색", description = "상품명으로 상품을 검색합니다")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "검색 키워드", example = "스마트폰") @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다 (관리자 전용)")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다 (관리자 전용)")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "상품 ID", example = "SEED-1-000001") @PathVariable String productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다 (논리 삭제, 관리자 전용)")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "상품 ID", example = "SEED-1-000001") @PathVariable String productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-init")
    @Operation(summary = "상품 대량 추가", description = "테스트용 상품을 대량으로 생성합니다 (관리자 전용)")
    public ResponseEntity<ProductBulkInitResponse> bulkInitProducts(
            @Valid @RequestBody(required = false) ProductBulkInitRequest request
    ) {
        ProductBulkInitRequest finalRequest = request != null
                ? request
                : new ProductBulkInitRequest();
        return ResponseEntity.ok(productService.bulkInitProducts(finalRequest));
    }
}