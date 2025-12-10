package org.example.user.service.performance;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsCollectionService {

    private final MeterRegistry meterRegistry;

    /**
     * JVM 메트릭 수집
     */
    public Map<String, Object> collectJvmMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        Map<String, Object> metrics = new HashMap<>();

        // 메모리 메트릭
        metrics.put("heap_used_mb", memoryBean.getHeapMemoryUsage().getUsed() / 1024 / 1024);
        metrics.put("heap_max_mb", memoryBean.getHeapMemoryUsage().getMax() / 1024 / 1024);
        metrics.put("heap_usage_percentage",
                (memoryBean.getHeapMemoryUsage().getUsed() * 100.0) / memoryBean.getHeapMemoryUsage().getMax());

        // 스레드 메트릭
        metrics.put("thread_count", threadBean.getThreadCount());
        metrics.put("peak_thread_count", threadBean.getPeakThreadCount());

        return metrics;
    }

    /**
     * 시스템 메트릭 수집
     */
    public Map<String, Object> collectSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("available_processors", runtime.availableProcessors());
        metrics.put("free_memory_mb", runtime.freeMemory() / 1024 / 1024);
        metrics.put("total_memory_mb", runtime.totalMemory() / 1024 / 1024);
        metrics.put("max_memory_mb", runtime.maxMemory() / 1024 / 1024);

        return metrics;
    }

    /**
     * 메트릭 수집 오버헤드 측정
     */
    public Map<String, Object> measureMetricsOverhead(int iterations) {
        // 메트릭 수집 없이 실행
        long withoutMetricsTotal = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            performSimpleOperation();
            withoutMetricsTotal += (System.nanoTime() - start);
        }
        long withoutMetricsAvg = withoutMetricsTotal / iterations / 1000;

        // 메트릭 수집하면서 실행
        long withMetricsTotal = 0;
        for (int i = 0; i < iterations; i++) {
            Timer.Sample sample = Timer.start(meterRegistry);
            long start = System.nanoTime();
            performSimpleOperation();
            long duration = System.nanoTime() - start;
            sample.stop(Timer.builder("test.metric")
                    .register(meterRegistry));
            withMetricsTotal += duration;
        }
        long withMetricsAvg = withMetricsTotal / iterations / 1000;

        double overheadPercentage = ((withMetricsAvg - withoutMetricsAvg) / (double) withoutMetricsAvg) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("iterations", iterations);
        result.put("without_metrics_avg_microseconds", withoutMetricsAvg);
        result.put("with_metrics_avg_microseconds", withMetricsAvg);
        result.put("overhead_microseconds", withMetricsAvg - withoutMetricsAvg);
        result.put("overhead_percentage", String.format("%.2f%%", overheadPercentage));

        return result;
    }

    /**
     * 간단한 연산 (테스트용)
     */
    private void performSimpleOperation() {
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i;
        }
    }
}