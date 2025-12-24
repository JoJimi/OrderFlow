package org.example.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.dto.response.ProductInfoResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Order Service 전용 Product 캐시 서비스
 * - Product Service와 독립적으로 캐시 관리
 * - Feign Client 호출 전 캐시 우선 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

    private static final String PRODUCT_CACHE_PREFIX = "order:product:";  // order: 접두사 추가
    private static final Duration PRODUCT_CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 상품 정보를 Redis에 캐싱
     */
    public void cacheProduct(String productId, ProductInfoResponse product) {
        try {
            String key = PRODUCT_CACHE_PREFIX + productId;
            String value = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(key, value, PRODUCT_CACHE_TTL);
            log.debug("상품 캐시 저장 완료: {}", productId);
        } catch (JsonProcessingException e) {
            log.error("상품 캐시 저장 중 JSON 변환 실패: {}", productId, e);
        }
    }

    /**
     * Redis에서 상품 정보 조회
     */
    public ProductInfoResponse getProductFromCache(String productId) {
        try {
            String key = PRODUCT_CACHE_PREFIX + productId;
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                log.debug("캐시 미스: {}", productId);
                return null;
            }

            log.debug("캐시 히트: {}", productId);
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
        log.debug("상품 캐시 삭제: {}", productId);
    }

    /**
     * 캐시 통계 정보 (디버깅용)
     */
    public long getCacheSize() {
        String pattern = PRODUCT_CACHE_PREFIX + "*";
        return redisTemplate.keys(pattern).size();
    }
}