package org.example.user.dto.performance;

import java.time.LocalDateTime;

public record ComparisonResult(
        String testName,
        Long baselineMs,
        Long optimizedMs,
        Double improvementPercentage,
        String speedup,
        LocalDateTime timestamp
) {
    public static ComparisonResult of(
            String testName,
            Long baselineMs,
            Long optimizedMs
    ) {
        double improvement = ((baselineMs - optimizedMs) / (double) baselineMs) * 100;
        String speedup = String.format("%.1fx", baselineMs / (double) optimizedMs);

        return new ComparisonResult(
                testName,
                baselineMs,
                optimizedMs,
                improvement,
                speedup,
                LocalDateTime.now()
        );
    }
}