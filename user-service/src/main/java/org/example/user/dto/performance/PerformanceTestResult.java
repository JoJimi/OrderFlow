package org.example.user.dto.performance;

import java.time.LocalDateTime;

public record PerformanceTestResult(
        String testName,
        Long executionTimeMs,
        Double throughput,
        Double successRate,
        LocalDateTime timestamp,
        String unit
) {
    public static PerformanceTestResult of(
            String testName,
            Long executionTimeMs,
            Double throughput,
            Double successRate
    ) {
        return new PerformanceTestResult(
                testName,
                executionTimeMs,
                throughput,
                successRate,
                LocalDateTime.now(),
                "ms"
        );
    }
}
