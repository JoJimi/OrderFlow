package org.example.user.controller.performance;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.dto.performance.ComparisonResult;
import org.example.user.repository.SpringDataUserRepository;
import org.example.user.security.jwt.JwtTokenProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/performance/auth")
@RequiredArgsConstructor
public class AuthPerformanceController {

    private final JwtTokenProvider jwtTokenProvider;
    private final SpringDataUserRepository userRepository;

    /**
     * JWT vs 세션(DB 조회) 인증 성능 비교
     * GET /api/performance/auth/compare
     */
    @GetMapping("/compare")
    public Map<String, Object> compareAuthMethods() {
        log.info("Starting authentication methods comparison");

        // 테스트용 JWT 토큰 생성 (Family ID 필요)
        String familyId = jwtTokenProvider.newFamilyId();
        String testToken = jwtTokenProvider.generateAccessToken("1", familyId);

        // JWT 검증 시간 측정 (10회 평균)
        List<Long> jwtTimes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            jwtTokenProvider.validateAccessToken(testToken);  // ✅ validateAccessToken 사용
            Claims claims = jwtTokenProvider.parseClaims(testToken);
            long end = System.nanoTime();
            jwtTimes.add((end - start) / 1_000_000); // 나노초 → 밀리초
        }
        long jwtAvg = jwtTimes.stream().mapToLong(Long::longValue).sum() / jwtTimes.size();

        // 세션 기반 인증 시뮬레이션 (DB 조회, 10회 평균)
        List<Long> sessionTimes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            userRepository.findById(1L);
            long end = System.nanoTime();
            sessionTimes.add((end - start) / 1_000_000);
        }
        long sessionAvg = sessionTimes.stream().mapToLong(Long::longValue).sum() / sessionTimes.size();

        // 비교 결과 생성
        ComparisonResult comparison = ComparisonResult.of(
                "JWT vs Session Authentication",
                sessionAvg,
                jwtAvg
        );

        Map<String, Object> result = new HashMap<>();
        result.put("test_name", "Authentication Performance Comparison");
        result.put("jwt_validation_avg_ms", jwtAvg);
        result.put("session_db_lookup_avg_ms", sessionAvg);
        result.put("improvement_percentage", comparison.improvementPercentage());
        result.put("speedup", comparison.speedup());
        result.put("description", "JWT validation is in-memory, session requires DB lookup");

        log.info("Comparison result - JWT: {}ms, Session(DB): {}ms, Speedup: {}",
                jwtAvg, sessionAvg, comparison.speedup());

        return result;
    }

    /**
     * JWT 검증 오버헤드 측정
     * GET /api/performance/auth/jwt-overhead
     */
    @GetMapping("/jwt-overhead")
    public Map<String, Object> measureJwtOverhead() {
        log.info("Measuring JWT validation overhead");

        String familyId = jwtTokenProvider.newFamilyId();
        String testToken = jwtTokenProvider.generateAccessToken("1", familyId);

        // 토큰 검증 없이 단순 연산 (1000회)
        long withoutValidation = 0;
        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            int sum = 0;
            for (int j = 0; j < 10; j++) sum += j;
            withoutValidation += (System.nanoTime() - start);
        }
        long withoutAvg = withoutValidation / 1000 / 1000; // 마이크로초

        // 토큰 검증 포함 (1000회)
        long withValidation = 0;
        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            jwtTokenProvider.validateAccessToken(testToken);  // ✅ validateAccessToken 사용
            int sum = 0;
            for (int j = 0; j < 10; j++) sum += j;
            withValidation += (System.nanoTime() - start);
        }
        long withAvg = withValidation / 1000 / 1000; // 마이크로초

        double overhead = ((withAvg - withoutAvg) / (double) withoutAvg) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("test_name", "JWT Validation Overhead");
        result.put("without_validation_microseconds", withoutAvg);
        result.put("with_validation_microseconds", withAvg);
        result.put("overhead_microseconds", withAvg - withoutAvg);
        result.put("overhead_percentage", String.format("%.2f%%", overhead));

        return result;
    }
}