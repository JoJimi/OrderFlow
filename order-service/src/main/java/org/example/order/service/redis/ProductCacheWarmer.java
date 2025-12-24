package org.example.order.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.client.ProductClient;
import org.example.order.dto.response.ProductInfoResponse;
import org.example.order.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 인기 상품 캐시 Pre-warming 스케줄러
 * - 주기적으로 인기 상품을 캐시에 미리 적재
 * - 캐시 히트율 향상 및 응답 시간 개선
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCacheWarmer {

    private final OrderRepository orderRepository;
    private final ProductCacheService productCacheService;
    private final ProductClient productClient;

    // 비동기 처리용 스레드 풀 (최대 10개 동시 처리)
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * ✅ 인기 상품 캐시 워밍업 (매 1시간마다 실행)
     * - 최근 7일간 주문이 많은 상품 Top 1000
     * - 애플리케이션 시작 후 1분 뒤 첫 실행
     */
    @Scheduled(
            initialDelay = 60_000,      // 1분 후 첫 실행
            fixedRate = 3_600_000       // 1시간마다 반복
    )
    public void warmUpPopularProducts() {
        log.info("===== 인기 상품 캐시 워밍업 시작 =====");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 최근 7일간 인기 상품 ID 조회 (Top 1000)
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<String> popularProductIds = orderRepository
                    .findPopularProductIds(since, 1000);

            if (popularProductIds.isEmpty()) {
                log.info("인기 상품이 없습니다. 캐시 워밍업 스킵");
                return;
            }

            log.info("인기 상품 {}개 발견, 캐시 적재 시작", popularProductIds.size());

            // 2. 비동기로 상품 정보 조회 및 캐싱
            List<CompletableFuture<Void>> futures = popularProductIds.stream()
                    .map(productId -> CompletableFuture.runAsync(() ->
                            warmUpProduct(productId), executorService))
                    .toList();

            // 3. 모든 작업 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .join();

            long duration = System.currentTimeMillis() - startTime;
            long cacheSize = productCacheService.getCacheSize();

            log.info("===== 인기 상품 캐시 워밍업 완료 =====");
            log.info("처리 시간: {}ms, 캐시 크기: {}개", duration, cacheSize);

            // 4. 캐시 통계 출력
            var stats = productCacheService.getCacheStats();
            log.info("캐시 통계: {}", stats);

        } catch (Exception e) {
            log.error("인기 상품 캐시 워밍업 중 오류 발생", e);
        }
    }

    /**
     * 개별 상품 캐시 워밍업
     */
    private void warmUpProduct(String productId) {
        try {
            // 1. 이미 캐시에 있는지 확인
            ProductInfoResponse cached = productCacheService.getProductFromCache(productId);
            if (cached != null) {
                log.debug("상품 {}는 이미 캐시됨, 스킵", productId);
                return;
            }

            // 2. Product Service에서 조회
            ProductInfoResponse product = productClient.getProduct(productId);

            if (product == null) {
                log.warn("상품 {}를 조회할 수 없음", productId);
                return;
            }

            // 3. 캐시에 저장
            productCacheService.cacheProduct(productId, product);
            log.debug("상품 {} 캐시 완료", productId);

        } catch (Exception e) {
            log.error("상품 {} 캐시 워밍업 실패", productId, e);
        }
    }

    /**
     * 수동 캐시 워밍업 (관리자가 API로 호출 가능)
     */
    public void warmUpNow() {
        log.info("수동 캐시 워밍업 요청");
        warmUpPopularProducts();
    }

    /**
     * 특정 상품 리스트 캐시 워밍업 (테스트용)
     */
    public void warmUpSpecificProducts(List<String> productIds) {
        log.info("특정 상품 {}개 캐시 워밍업 시작", productIds.size());

        productIds.forEach(this::warmUpProduct);

        log.info("특정 상품 캐시 워밍업 완료");
    }

    /**
     * 애플리케이션 종료 시 스레드 풀 정리
     */
    public void shutdown() {
        log.info("ProductCacheWarmer 스레드 풀 종료");
        executorService.shutdown();
    }
}