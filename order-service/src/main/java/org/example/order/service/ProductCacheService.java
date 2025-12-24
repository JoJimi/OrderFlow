package org.example.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.dto.cache.CacheStats;
import org.example.order.dto.response.ProductInfoResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Order Service 전용 Product 캐시 서비스
 * - Product Service와 독립적으로 캐시 관리
 * - Feign Client 호출 전 캐시 우선 조회
 * - 캐시 히트율 측정 및 모니터링
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

    private static final String PRODUCT_CACHE_PREFIX = "order:product:";
    private static final Duration PRODUCT_CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // 캐시 히트율 측정용 카운터
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong cacheMissCount = new AtomicLong(0);

    /**
     * 상품 정보를 Redis에 캐싱
     */
    public void cacheProduct(String productId, ProductInfoResponse product) {
        try {
            String key = PRODUCT_CACHE_PREFIX + productId;
            String value = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(key, value, PRODUCT_CACHE_TTL);
            log.info("상품 캐시 저장 완료: {}", productId);
        } catch (JsonProcessingException e) {
            log.error("상품 캐시 저장 중 JSON 변환 실패: {}", productId, e);
        }
    }

    /**
     * Redis에서 상품 정보 조회 (히트율 측정 포함)
     */
    public ProductInfoResponse getProductFromCache(String productId) {
        try {
            String key = PRODUCT_CACHE_PREFIX + productId;
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                cacheMissCount.incrementAndGet();
                log.info("캐시 미스: {} (히트율: {:.2f}%)",
                        productId, getCacheHitRatio());
                return null;
            }

            cacheHitCount.incrementAndGet();
            log.info("캐시 히트: {} (히트율: {:.2f}%)",
                    productId, getCacheHitRatio());
            return objectMapper.readValue(value, ProductInfoResponse.class);
        } catch (JsonProcessingException e) {
            log.error("상품 캐시 조회 중 JSON 변환 실패: {}", productId, e);
            return null;
        }
    }

    /**
     * 특정 상품의 캐시 삭제
     */
    public void evictProduct(String productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.delete(key);
        log.info("상품 캐시 삭제: {}", productId);
    }

    /**
     * 전체 캐시 삭제 (테스트/유지보수용)
     */
    public void evictAll() {
        String pattern = PRODUCT_CACHE_PREFIX + "*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("전체 상품 캐시 삭제 완료: {}개", keys.size());
        }
    }

    /**
     * 캐시 통계 정보 (디버깅용)
     */
    public long getCacheSize() {
        String pattern = PRODUCT_CACHE_PREFIX + "*";
        var keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /**
     * 캐시 히트율 조회 (백분율)
     */
    public double getCacheHitRatio() {
        long hits = cacheHitCount.get();
        long misses = cacheMissCount.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total * 100.0;
    }

    /**
     * 캐시 통계 조회 (DTO)
     */
    public CacheStats getCacheStats() {
        long hits = cacheHitCount.get();
        long misses = cacheMissCount.get();
        long total = hits + misses;
        double hitRatio = total > 0 ? (double) hits / total * 100.0 : 0.0;
        long cacheSize = getCacheSize();

        return new CacheStats(hits, misses, total, hitRatio, cacheSize);
    }

    /**
     * 캐시 통계 초기화 (테스트용)
     */
    public void resetStats() {
        cacheHitCount.set(0);
        cacheMissCount.set(0);
        log.info("캐시 통계 초기화 완료");
    }
}