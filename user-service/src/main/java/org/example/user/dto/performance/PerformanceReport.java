package org.example.user.dto.performance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record PerformanceReport(
        String reportTitle,
        LocalDateTime generatedAt,
        Map<String, Object> summary,
        List<ComparisonResult> comparisons,
        List<String> recommendations,
        Map<String, String> systemInfo
) {
    public static PerformanceReport create(
            String title,
            Map<String, Object> summary,
            List<ComparisonResult> comparisons,
            List<String> recommendations
    ) {
        Map<String, String> systemInfo = Map.of(
                "javaVersion", System.getProperty("java.version"),
                "osName", System.getProperty("os.name"),
                "availableProcessors", String.valueOf(Runtime.getRuntime().availableProcessors())
        );

        return new PerformanceReport(
                title,
                LocalDateTime.now(),
                summary,
                comparisons,
                recommendations,
                systemInfo
        );
    }
}