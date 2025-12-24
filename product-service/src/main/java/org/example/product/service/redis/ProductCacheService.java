package org.example.product.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.dto.response.ProductResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final Duration PRODUCT_CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 상품 정보를 Redis에 캐싱합니다.
     */
    public void cacheProduct(String productId, ProductResponse product) {
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
     * Redis에서 상품 정보를 조회합니다.
     */
    public ProductResponse getProductFromCache(String productId) {
        try {
            String key = PRODUCT_CACHE_PREFIX + productId;
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) return null;
            return objectMapper.readValue(value, ProductResponse.class);
        } catch (JsonProcessingException e) {
            log.error("상품 캐시 조회 중 JSON 변환 실패: {}", productId, e);
            return null;
        }
    }

    /**
     * 특정 상품의 캐시를 삭제합니다.
     */
    public void evictProduct(String productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.delete(key);
        log.debug("상품 캐시 삭제: {}", productId);
    }

    /**
     * 모든 상품 캐시를 삭제합니다.
     */
    public void evictAllProducts() {
        String pattern = PRODUCT_CACHE_PREFIX + "*";
        redisTemplate.keys(pattern)
                .forEach(key -> redisTemplate.delete(key));
        log.info("모든 상품 캐시 삭제 완료");
    }

    /**
     * 특정 상품의 캐시 만료 시간을 연장합니다.
     */
    public void refreshTTL(String productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.expire(key, PRODUCT_CACHE_TTL);
        log.debug("상품 캐시 TTL 갱신: {}", productId);
    }
}