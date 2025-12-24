package org.example.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.dto.cache.CacheStats;
import org.example.order.service.ProductCacheService;
import org.example.order.service.ProductCacheWarmer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 캐시 관리 및 모니터링 API
 * - 캐시 통계 조회
 * - 수동 워밍업
 * - 캐시 초기화
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheManagementController {

    private final ProductCacheService productCacheService;
    private final ProductCacheWarmer productCacheWarmer;

    /**
     * 캐시 통계 조회 (누구나 가능)
     */
    @GetMapping("/stats")
    public ResponseEntity<CacheStats> getCacheStats() {
        log.info("캐시 통계 조회 요청");
        var stats = productCacheService.getCacheStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 캐시 히트율 조회 (누구나 가능)
     */
    @GetMapping("/hit-ratio")
    public ResponseEntity<Map<String, Object>> getCacheHitRatio() {
        log.info("캐시 히트율 조회 요청");
        double hitRatio = productCacheService.getCacheHitRatio();
        long cacheSize = productCacheService.getCacheSize();

        return ResponseEntity.ok(Map.of(
                "hitRatio", hitRatio,
                "cacheSize", cacheSize,
                "message", String.format("캐시 히트율: %.2f%%, 캐시 크기: %d개", hitRatio, cacheSize)
        ));
    }

    /**
     * 수동 캐시 워밍업 (관리자 전용)
     */
    @PostMapping("/warm-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> warmUpCache() {
        log.info("수동 캐시 워밍업 요청");

        // 비동기로 실행 (응답 즉시 반환)
        new Thread(() -> productCacheWarmer.warmUpNow()).start();

        return ResponseEntity.ok(Map.of(
                "message", "캐시 워밍업이 백그라운드에서 시작되었습니다.",
                "status", "processing"
        ));
    }

    /**
     * 특정 상품 캐시 워밍업 (관리자 전용)
     */
    @PostMapping("/warm-up/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> warmUpSpecificProducts(
            @RequestBody List<String> productIds) {
        log.info("특정 상품 {}개 캐시 워밍업 요청", productIds.size());

        productCacheWarmer.warmUpSpecificProducts(productIds);

        return ResponseEntity.ok(Map.of(
                "message", String.format("%d개 상품 캐시 워밍업 완료", productIds.size()),
                "status", "completed"
        ));
    }

    /**
     * 캐시 통계 초기화 (관리자 전용, 테스트용)
     */
    @PostMapping("/stats/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetCacheStats() {
        log.info("캐시 통계 초기화 요청");

        productCacheService.resetStats();

        return ResponseEntity.ok(Map.of(
                "message", "캐시 통계가 초기화되었습니다.",
                "status", "reset"
        ));
    }

    /**
     * 전체 캐시 삭제 (관리자 전용, 긴급 상황용)
     */
    @DeleteMapping("/evict-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> evictAllCache() {
        log.warn("전체 캐시 삭제 요청 - 관리자 권한");

        long beforeSize = productCacheService.getCacheSize();
        productCacheService.evictAll();

        return ResponseEntity.ok(Map.of(
                "message", String.format("%d개 캐시가 삭제되었습니다.", beforeSize),
                "status", "deleted"
        ));
    }

    /**
     * 특정 상품 캐시 삭제 (관리자 전용)
     */
    @DeleteMapping("/evict/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> evictProduct(
            @PathVariable String productId) {
        log.info("상품 {} 캐시 삭제 요청", productId);

        productCacheService.evictProduct(productId);

        return ResponseEntity.ok(Map.of(
                "message", String.format("상품 %s 캐시가 삭제되었습니다.", productId),
                "productId", productId,
                "status", "deleted"
        ));
    }
}