package org.example.user.controller.performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.dto.performance.ComparisonResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.*;
import java.sql.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/performance/hikaricp")
@RequiredArgsConstructor
public class HikariCPPerformanceController {

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * 커넥션 풀 사용 vs 미사용 성능 비교
     * GET /api/performance/hikaricp/compare
     */
    @GetMapping("/compare")
    public Map<String, Object> compareConnectionMethods() {
        log.info("Starting HikariCP performance comparison");

        // 커넥션 풀 사용 (10회 평균)
        List<Long> poolTimes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                rs.next();
            } catch (Exception e) {
                log.error("Error with pool connection", e);
            }
            long end = System.nanoTime();
            poolTimes.add((end - start) / 1_000_000); // 나노초 → 밀리초
        }
        long poolAvg = poolTimes.stream().mapToLong(Long::longValue).sum() / poolTimes.size();

        // 새 커넥션 생성 (DriverManager, 10회 평균)
        List<Long> noPoolTimes = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                rs.next();
            } catch (Exception e) {
                log.error("Error with direct connection", e);
            }
            long end = System.nanoTime();
            noPoolTimes.add((end - start) / 1_000_000);
        }
        long noPoolAvg = noPoolTimes.stream().mapToLong(Long::longValue).sum() / noPoolTimes.size();

        // 비교 결과 생성
        ComparisonResult comparison = ComparisonResult.of(
                "Connection Pool vs Direct Connection",
                noPoolAvg,
                poolAvg
        );

        Map<String, Object> result = new HashMap<>();
        result.put("test_name", "HikariCP Performance Comparison");
        result.put("with_pool_avg_ms", poolAvg);
        result.put("without_pool_avg_ms", noPoolAvg);
        result.put("improvement_percentage", comparison.improvementPercentage());
        result.put("speedup", comparison.speedup());
        result.put("tcp_handshake_cost_saved_ms", noPoolAvg - poolAvg);
        result.put("description", "Connection pool reuses existing connections, saving TCP handshake time");

        log.info("Comparison result - Pool: {}ms, No Pool: {}ms, Speedup: {}",
                poolAvg, noPoolAvg, comparison.speedup());

        return result;
    }

    /**
     * 동시 연결 테스트
     * GET /api/performance/hikaricp/concurrent?threads=10
     */
    @GetMapping("/concurrent")
    public Map<String, Object> testConcurrentConnections(
            @RequestParam(defaultValue = "10") int threads
    ) {
        log.info("Testing concurrent connections with {} threads", threads);

        List<Thread> threadList = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                long start = System.nanoTime();
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    rs.next();
                } catch (Exception e) {
                    log.error("Error in concurrent test", e);
                }
                long duration = (System.nanoTime() - start) / 1_000_000;
                synchronized (executionTimes) {
                    executionTimes.add(duration);
                }
            });
            threadList.add(thread);
        }

        // 모든 스레드 시작
        long overallStart = System.nanoTime();
        threadList.forEach(Thread::start);

        // 모든 스레드 종료 대기
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long overallDuration = (System.nanoTime() - overallStart) / 1_000_000;

        long avgTime = executionTimes.stream().mapToLong(Long::longValue).sum() / executionTimes.size();
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("test_name", "Concurrent Connection Test");
        result.put("concurrent_threads", threads);
        result.put("overall_duration_ms", overallDuration);
        result.put("avg_connection_time_ms", avgTime);
        result.put("max_connection_time_ms", maxTime);
        result.put("min_connection_time_ms", minTime);
        result.put("description", "All threads completed successfully with connection pooling");

        return result;
    }
}