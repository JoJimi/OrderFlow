package org.example.order.dto.redis;

public record CacheStats(
        long hitCount,
        long missCount,
        long totalRequests,
        double hitRatio,
        long cacheSize
) {
    @Override
    public String toString() {
        return String.format(
                "CacheStats[hits=%d, misses=%d, total=%d, hitRatio=%.2f%%, size=%d]",
                hitCount, missCount, totalRequests, hitRatio, cacheSize
        );
    }
}
