package org.example.user.service.performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.domain.User;
import org.example.user.dto.cache.UserCacheDto;
import org.example.user.repository.SpringDataUserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceTestService {

    private final SpringDataUserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * DB에서 사용자 조회 (캐시 없이)
     */
    public Optional<User> findUserWithoutCache(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Redis 캐시를 통한 사용자 조회
     */
    public User findUserWithCache(Long userId) {
        String cacheKey = "user:" + userId;

        // Redis에서 먼저 조회
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 캐시 미스 시 DB 조회 후 캐시 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        redisTemplate.opsForValue().set(cacheKey, user);

        return user;
    }

    /**
     * DB 조회 지연 시간 측정 (10회 평균)
     */
    public long measureDatabaseLatency(Long userId) {
        List<Long> latencies = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            userRepository.findById(userId);
            long end = System.nanoTime();
            latencies.add((end - start) / 1_000_000); // 나노초 → 밀리초
        }

        return latencies.stream()
                .mapToLong(Long::longValue)
                .sum() / latencies.size();
    }

    /**
     * Redis 조회 지연 시간 측정 (10회 평균)
     */
    public long measureRedisLatency(Long userId) {
        String cacheKey = "user:" + userId;

        // User 엔티티 → UserCacheDto 변환 후 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserCacheDto cacheDto = UserCacheDto.from(user);
        redisTemplate.opsForValue().set(cacheKey, cacheDto);

        List<Long> latencies = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            redisTemplate.opsForValue().get(cacheKey);
            long end = System.nanoTime();
            latencies.add((end - start) / 1_000_000);
        }

        return latencies.stream()
                .mapToLong(Long::longValue)
                .sum() / latencies.size();
    }

    /**
     * 캐시 히트율 측정
     */
    public double measureCacheHitRate(Long userId, int totalRequests) {
        String cacheKey = "user:" + userId;
        int cacheHits = 0;

        // 첫 번째 요청: 캐시 미스 (DB 조회 후 캐시 저장)
        findUserWithCache(userId);

        // 이후 요청: 캐시 히트 예상
        for (int i = 0; i < totalRequests - 1; i++) {
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                cacheHits++;
            }
        }

        return (cacheHits / (double) (totalRequests - 1)) * 100;
    }
}