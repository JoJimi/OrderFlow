package org.example.user.controller.performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.dto.performance.ComparisonResult;
import org.example.user.service.performance.PerformanceTestService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/performance/redis")
@RequiredArgsConstructor
public class RedisPerformanceController {

    private final PerformanceTestService performanceTestService;

    /**
     * PostgreSQL vs Redis 성능 비교
     * GET /api/performance/redis/compare?userId=1
     */
    @GetMapping("/compare")
    public Map<String, Object> comparePerformance(@RequestParam Long userId) {
        log.info("Starting Redis performance comparison for userId: {}", userId);

        // PostgreSQL 조회 지연 시간 측정
        long dbLatency = performanceTestService.measureDatabaseLatency(userId);

        // Redis 조회 지연 시간 측정
        long redisLatency = performanceTestService.measureRedisLatency(userId);

        // 비교 결과 생성
        ComparisonResult comparison = ComparisonResult.of(
                "PostgreSQL vs Redis Latency",
                dbLatency,
                redisLatency
        );

        Map<String, Object> result = new HashMap<>();
        result.put("test_name", "Redis Performance Comparison");
        result.put("postgresql_avg_ms", dbLatency);
        result.put("redis_avg_ms", redisLatency);
        result.put("improvement_percentage", comparison.improvementPercentage());
        result.put("speedup", comparison.speedup());
        result.put("description", "PostgreSQL takes " + dbLatency + "ms, Redis takes " + redisLatency + "ms");

        log.info("Comparison result - DB: {}ms, Redis: {}ms, Speedup: {}",
                dbLatency, redisLatency, comparison.speedup());

        return result;
    }

    /**
     * 캐시 히트율 측정
     * GET /api/performance/redis/cache-hit-rate?userId=1&requests=100
     */
    @GetMapping("/cache-hit-rate")
    public Map<String, Object> testCacheHitRate(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "100") int requests
    ) {
        log.info("Measuring cache hit rate for userId: {} with {} requests", userId, requests);

        double hitRate = performanceTestService.measureCacheHitRate(userId, requests);

        Map<String, Object> result = new HashMap<>();
        result.put("test_name", "Cache Hit Rate Test");
        result.put("user_id", userId);
        result.put("total_requests", requests);
        result.put("cache_hit_rate_percentage", String.format("%.2f%%", hitRate));
        result.put("description", "First request is cache miss, subsequent requests should be cache hits");

        return result;
    }
}