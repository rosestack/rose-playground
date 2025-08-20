package io.github.rosestack.crypto;

import io.github.rosestack.crypto.enums.EncryptType;
import io.github.rosestack.crypto.monitor.CryptoMonitorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

/**
 * 监控功能演示测试
 *
 * 这个测试展示了如何使用新的监控功能来跟踪加密操作的性能和统计信息
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("监控功能演示")
class MonitoringDemoTest {

    private static final String SECRET_KEY = "MySecretKey12345MySecretKey12345"; // 32字符

    @BeforeEach
    void setUp() {
        // 清空之前的统计数据
        CryptoMonitorManager.getInstance().clearCache();
    }

    @Test
    @DisplayName("完整监控功能演示")
    void demonstrateMonitoringFeatures() {
        System.out.println("=== Rose Encryption 监控功能演示 ===\n");

        // 1. 执行各种加密操作
        System.out.println("1. 执行加密操作...");

        // 不同算法
        String text1 = CryptoUtils.encrypt("Hello AES", EncryptType.AES, SECRET_KEY);
        String text2 = CryptoUtils.encrypt("Hello DES", EncryptType.DES, SECRET_KEY);
        String text3 = CryptoUtils.encrypt("Hello 3DES", EncryptType.DES3, SECRET_KEY);

        // 不同大小的数据
        String smallData = CryptoUtils.encrypt("Small", EncryptType.AES, SECRET_KEY);
        String mediumData = CryptoUtils.encrypt("This is a medium length text for testing encryption performance and monitoring capabilities", EncryptType.AES, SECRET_KEY);

        StringBuilder largeDataBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeDataBuilder.append("Large data chunk ").append(i).append(" ");
        }
        String largeData = CryptoUtils.encrypt(largeDataBuilder.toString(), EncryptType.AES, SECRET_KEY);

        // 解密操作
        CryptoUtils.decrypt(text1, EncryptType.AES, SECRET_KEY);
        CryptoUtils.decrypt(text2, EncryptType.DES, SECRET_KEY);
        CryptoUtils.decrypt(smallData, EncryptType.AES, SECRET_KEY);

        // 故意触发一些错误
        try {
            CryptoUtils.decrypt("invalid_base64", EncryptType.AES, SECRET_KEY);
        } catch (Exception e) {
            // 预期的错误
        }

        try {
            CryptoUtils.encrypt("test", EncryptType.AES, "short"); // 短密钥
        } catch (Exception e) {
            // 预期的错误
        }

        System.out.println("   ✓ 完成各种加密解密操作\n");

        // 2. 展示基础性能统计
        System.out.println("2. 基础性能统计:");
        Map<String, Long> performanceStats = CryptoMonitorManager.getInstance().getPerformanceStats();
        performanceStats.entrySet().stream()
            .filter(entry -> entry.getKey().contains("success") || entry.getKey().contains("failure"))
            .forEach(entry -> System.out.printf("   %s: %d%n", entry.getKey(), entry.getValue()));
        System.out.println();

        // 3. 展示算法使用统计
        System.out.println("3. 算法使用统计:");
        Map<String, Long> algorithmStats = CryptoMonitorManager.getInstance().getAlgorithmUsageStats();
        algorithmStats.forEach((algorithm, count) ->
            System.out.printf("   %s: %d 次%n", algorithm, count));
        System.out.println();

        // 4. 展示错误统计
        System.out.println("4. 错误统计:");
        Map<String, Long> errorStats = CryptoMonitorManager.getInstance().getErrorStats();
        if (errorStats.isEmpty()) {
            System.out.println("   无错误记录");
        } else {
            errorStats.forEach((errorType, count) ->
                System.out.printf("   %s: %d 次%n", errorType, count));
        }
        System.out.println();

        // 5. 展示数据大小分布
        System.out.println("5. 数据大小分布:");
        Map<String, Long> dataSizeStats = CryptoMonitorManager.getInstance().getDataSizeStats();
        dataSizeStats.forEach((sizeCategory, count) ->
            System.out.printf("   %s: %d 次%n", sizeCategory, count));
        System.out.println();

        // 6. 展示完整监控报告
        System.out.println("6. 完整监控报告:");
        Map<String, Object> report = CryptoMonitorManager.getInstance().getMonitoringReport();

        @SuppressWarnings("unchecked")
        Map<String, Double> successRates = (Map<String, Double>) report.get("successRates");
        System.out.println("   成功率统计:");
        successRates.forEach((operation, rate) ->
            System.out.printf("     %s: %.1f%%%n", operation, rate));

        @SuppressWarnings("unchecked")
        Map<String, Object> systemInfo = (Map<String, Object>) report.get("systemInfo");
        System.out.printf("   总操作数: %s%n", systemInfo.get("totalOperations"));
        System.out.printf("   支持的算法: %s%n", systemInfo.get("supportedAlgorithms"));
        System.out.println();

        // 7. 展示缓存统计
        System.out.println("7. 缓存统计:");
        String cacheStats = CryptoMonitorManager.getInstance().getCacheStats();
        System.out.printf("   %s%n", cacheStats);
        System.out.println();

        System.out.println("=== 监控功能演示完成 ===");
    }

    @Test
    @DisplayName("性能基准测试")
    void performanceBenchmark() {
        System.out.println("=== 性能基准测试 ===\n");

        int iterations = 1000;
        String testData = "Performance test data for encryption benchmarking";

        System.out.printf("执行 %d 次 AES 加密解密操作...%n", iterations);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            String encrypted = CryptoUtils.encrypt(testData, EncryptType.AES, SECRET_KEY);
            CryptoUtils.decrypt(encrypted, EncryptType.AES, SECRET_KEY);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("完成时间: %d ms%n", duration);
        System.out.printf("平均每次操作: %.2f ms%n", (double) duration / (iterations * 2));
        System.out.printf("吞吐量: %.0f 操作/秒%n", (iterations * 2.0 * 1000) / duration);

        // 显示详细统计
        Map<String, Object> report = CryptoMonitorManager.getInstance().getMonitoringReport();
        @SuppressWarnings("unchecked")
        Map<String, Long> performance = (Map<String, Long>) report.get("performance");

        System.out.println("\n详细性能统计:");
        performance.entrySet().stream()
            .filter(entry -> entry.getKey().contains("AES") && entry.getKey().contains("time"))
            .forEach(entry -> {
                String key = entry.getKey();
                Long value = entry.getValue();
                if (key.contains("total_time")) {
                    System.out.printf("   %s: %.2f ms%n", key, value / 1_000_000.0);
                } else if (key.contains("avg_time")) {
                    System.out.printf("   %s: %.3f ms%n", key, value / 1_000_000.0);
                }
            });

        System.out.println("\n=== 性能基准测试完成 ===");
    }
}
