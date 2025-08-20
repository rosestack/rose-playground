package io.github.rosestack.encryption.monitor;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 加密服务监控管理器
 *
 * <p>负责收集和管理加密服务的各种监控指标，包括：
 * <ul>
 *   <li>性能统计（操作计数、执行时间）</li>
 *   <li>算法使用统计</li>
 *   <li>错误统计</li>
 *   <li>数据大小分布统计</li>
 *   <li>成功率计算</li>
 * </ul>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class EncryptionMonitorManager {

    /**
     * 单例实例
     */
    private static final EncryptionMonitorManager INSTANCE = new EncryptionMonitorManager();

    /**
     * 性能统计
     */
    private final ConcurrentMap<String, AtomicLong> performanceCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> performanceTimes = new ConcurrentHashMap<>();

    /**
     * 监控指标
     */
    private final ConcurrentMap<String, AtomicLong> algorithmUsage = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> dataSizeStats = new ConcurrentHashMap<>();

    /**
     * 私有构造函数
     */
    private EncryptionMonitorManager() {
    }

    /**
     * 获取单例实例
     */
    public static EncryptionMonitorManager getInstance() {
        return INSTANCE;
    }

    /**
     * 记录性能统计
     */
    public void recordPerformance(String operation, String encryptType, long startTime, boolean success, int dataSize) {
        long duration = System.nanoTime() - startTime;

        // 基础性能统计
        String key = operation + "_" + encryptType + (success ? "_success" : "_failure");
        performanceCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        performanceTimes.computeIfAbsent(operation + "_" + encryptType + "_total_time", k -> new AtomicLong(0)).addAndGet(duration);

        // 算法使用统计
        algorithmUsage.computeIfAbsent(encryptType, k -> new AtomicLong(0)).incrementAndGet();

        // 数据大小统计
        String sizeCategory = categorizeDataSize(dataSize);
        dataSizeStats.computeIfAbsent(operation + "_" + sizeCategory, k -> new AtomicLong(0)).incrementAndGet();

        // 记录平均处理时间
        String avgKey = operation + "_" + encryptType + "_avg_time";
        long currentCount = performanceCounters.getOrDefault(key, new AtomicLong(0)).get();
        if (currentCount > 0) {
            long totalTime = performanceTimes.getOrDefault(operation + "_" + encryptType + "_total_time", new AtomicLong(0)).get();
            long avgTime = totalTime / currentCount;
            performanceTimes.put(avgKey, new AtomicLong(avgTime));
        }
    }

    /**
     * 记录错误统计
     */
    public void recordError(String operation, String encryptType, String errorType) {
        String errorKey = operation + "_" + encryptType + "_" + errorType;
        errorCounters.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();

        // 记录总错误数
        String totalErrorKey = operation + "_total_errors";
        errorCounters.computeIfAbsent(totalErrorKey, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 数据大小分类
     */
    private String categorizeDataSize(int size) {
        if (size <= 100) return "small";
        if (size <= 1000) return "medium";
        if (size <= 10000) return "large";
        return "xlarge";
    }

    /**
     * 获取性能统计
     */
    public Map<String, Long> getPerformanceStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        performanceCounters.forEach((key, value) -> stats.put(key, value.get()));
        performanceTimes.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 获取算法使用统计
     */
    public Map<String, Long> getAlgorithmUsageStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        algorithmUsage.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 获取错误统计
     */
    public Map<String, Long> getErrorStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        errorCounters.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 获取数据大小统计
     */
    public Map<String, Long> getDataSizeStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        dataSizeStats.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 获取完整的监控报告
     */
    public Map<String, Object> getMonitoringReport() {
        Map<String, Object> report = new ConcurrentHashMap<>();

        // 基础性能统计
        report.put("performance", getPerformanceStats());

        // 算法使用统计
        report.put("algorithmUsage", getAlgorithmUsageStats());

        // 错误统计
        report.put("errors", getErrorStats());

        // 数据大小统计
        report.put("dataSizeDistribution", getDataSizeStats());

        // 计算成功率
        Map<String, Double> successRates = calculateSuccessRates();
        report.put("successRates", successRates);

        // 系统信息
        Map<String, Object> systemInfo = new ConcurrentHashMap<>();
        systemInfo.put("timestamp", System.currentTimeMillis());
        systemInfo.put("supportedAlgorithms", algorithmUsage.keySet());
        systemInfo.put("totalOperations", getTotalOperations());
        report.put("systemInfo", systemInfo);

        return report;
    }

    /**
     * 计算成功率
     */
    private Map<String, Double> calculateSuccessRates() {
        Map<String, Double> successRates = new ConcurrentHashMap<>();

        performanceCounters.entrySet().stream()
            .filter(entry -> entry.getKey().endsWith("_success"))
            .forEach(successEntry -> {
                String baseKey = successEntry.getKey().replace("_success", "");
                String failureKey = baseKey + "_failure";

                long successCount = successEntry.getValue().get();
                long failureCount = performanceCounters.getOrDefault(failureKey, new AtomicLong(0)).get();
                long totalCount = successCount + failureCount;

                if (totalCount > 0) {
                    double successRate = (double) successCount / totalCount * 100;
                    successRates.put(baseKey, successRate);
                }
            });

        return successRates;
    }

    /**
     * 获取总操作数
     */
    private long getTotalOperations() {
        return performanceCounters.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format(
            "性能计数器: %d, 性能时间统计: %d, 算法使用统计: %d, 错误统计: %d, 数据大小统计: %d",
            performanceCounters.size(),
            performanceTimes.size(),
            algorithmUsage.size(),
            errorCounters.size(),
            dataSizeStats.size());
    }

    /**
     * 清空缓存和统计
     */
    public void clearCache() {
        performanceCounters.clear();
        performanceTimes.clear();
        algorithmUsage.clear();
        errorCounters.clear();
        dataSizeStats.clear();
        log.info("已清空加密工具缓存和统计");
    }
}
