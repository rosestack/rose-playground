package io.github.rosestack.encryption;

import io.github.rosestack.encryption.enums.EncryptType;
import io.github.rosestack.encryption.monitor.EncryptionMonitorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 加密性能基准测试
 *
 * 注意：这些测试主要用于性能分析，在CI环境中可能需要禁用
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("加密性能基准测试")
@Disabled("性能测试，仅在需要时手动运行")
class EncryptionPerformanceTest {

    private static final String SECRET_KEY = "MySecretKey12345MySecretKey12345";
    private static final String SHORT_TEXT = "Hello";
    private static final String MEDIUM_TEXT = "This is a medium length text for testing encryption performance.";
    private static final String LONG_TEXT = generateLongText(1000);

    private DefaultFieldEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new DefaultFieldEncryptor(SECRET_KEY, true);
        // 清空性能统计
        EncryptionMonitorManager.getInstance().clearCache();
    }

    @Test
    @DisplayName("AES加密性能基准测试")
    void testAESEncryptionPerformance() {
        int iterations = 10000;

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String encrypted = encryptor.encrypt(MEDIUM_TEXT, EncryptType.AES);
            encryptor.decrypt(encrypted, EncryptType.AES);
        }
        long endTime = System.nanoTime();

        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double operationsPerSecond = (iterations * 2.0 * 1000) / durationMs; // 2 operations per iteration

        System.out.printf("AES性能测试结果：%d次操作耗时%dms，平均%.2f操作/秒%n",
                         iterations * 2, durationMs, operationsPerSecond);

        // 性能断言（这些值需要根据实际环境调整）
        assertTrue(operationsPerSecond > 1000, "AES加密性能应该超过1000操作/秒");
        assertTrue(durationMs < 30000, "10000次AES加密解密应该在30秒内完成");
    }

    @Test
    @DisplayName("不同文本长度性能对比")
    void testPerformanceWithDifferentTextLengths() {
        int iterations = 1000;
        String[] texts = {SHORT_TEXT, MEDIUM_TEXT, LONG_TEXT};
        String[] labels = {"短文本", "中等文本", "长文本"};

        for (int i = 0; i < texts.length; i++) {
            String text = texts[i];
            String label = labels[i];

            long startTime = System.nanoTime();
            for (int j = 0; j < iterations; j++) {
                String encrypted = encryptor.encrypt(text, EncryptType.AES);
                encryptor.decrypt(encrypted, EncryptType.AES);
            }
            long endTime = System.nanoTime();

            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            double operationsPerSecond = (iterations * 2.0 * 1000) / durationMs;

            System.out.printf("%s(%d字符)：%d次操作耗时%dms，平均%.2f操作/秒%n",
                             label, text.length(), iterations * 2, durationMs, operationsPerSecond);
        }
    }

    @Test
    @DisplayName("不同加密算法性能对比")
    void testPerformanceComparisonBetweenAlgorithms() {
        int iterations = 1000;
        EncryptType[] algorithms = {EncryptType.AES, EncryptType.DES, EncryptType.DES3};

        for (EncryptType algorithm : algorithms) {
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                String encrypted = encryptor.encrypt(MEDIUM_TEXT, algorithm);
                encryptor.decrypt(encrypted, algorithm);
            }
            long endTime = System.nanoTime();

            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            double operationsPerSecond = (iterations * 2.0 * 1000) / durationMs;

            System.out.printf("%s算法：%d次操作耗时%dms，平均%.2f操作/秒%n",
                             algorithm, iterations * 2, durationMs, operationsPerSecond);
        }
    }

    @Test
    @DisplayName("并发性能测试")
    void testConcurrentPerformance() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 1000;
        Thread[] threads = new Thread[threadCount];
        long[] threadTimes = new long[threadCount];

        long overallStartTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                long threadStartTime = System.nanoTime();
                for (int j = 0; j < operationsPerThread; j++) {
                    String encrypted = encryptor.encrypt(MEDIUM_TEXT, EncryptType.AES);
                    encryptor.decrypt(encrypted, EncryptType.AES);
                }
                threadTimes[threadIndex] = System.nanoTime() - threadStartTime;
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        long overallEndTime = System.nanoTime();
        long overallDurationMs = TimeUnit.NANOSECONDS.toMillis(overallEndTime - overallStartTime);

        // 计算统计信息
        long totalOperations = threadCount * operationsPerThread * 2L;
        double overallOperationsPerSecond = (totalOperations * 1000.0) / overallDurationMs;

        long avgThreadTime = 0;
        for (long threadTime : threadTimes) {
            avgThreadTime += TimeUnit.NANOSECONDS.toMillis(threadTime);
        }
        avgThreadTime /= threadCount;

        System.out.printf("并发性能测试结果：%n");
        System.out.printf("  线程数：%d%n", threadCount);
        System.out.printf("  每线程操作数：%d%n", operationsPerThread * 2);
        System.out.printf("  总操作数：%d%n", totalOperations);
        System.out.printf("  总耗时：%dms%n", overallDurationMs);
        System.out.printf("  平均线程耗时：%dms%n", avgThreadTime);
        System.out.printf("  整体吞吐量：%.2f操作/秒%n", overallOperationsPerSecond);

        // 性能断言
        assertTrue(overallOperationsPerSecond > 5000, "并发性能应该超过5000操作/秒");
    }

    @Test
    @DisplayName("内存使用测试")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // 强制垃圾回收
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 执行大量加密操作
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            String encrypted = encryptor.encrypt(MEDIUM_TEXT, EncryptType.AES);
            encryptor.decrypt(encrypted, EncryptType.AES);
        }

        // 再次检查内存使用
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        System.out.printf("内存使用测试结果：%n");
        System.out.printf("  初始内存：%d KB%n", initialMemory / 1024);
        System.out.printf("  最终内存：%d KB%n", finalMemory / 1024);
        System.out.printf("  内存增长：%d KB%n", memoryIncrease / 1024);
        System.out.printf("  平均每操作内存：%.2f bytes%n", (double) memoryIncrease / (iterations * 2));

        // 内存使用不应该过度增长
        assertTrue(memoryIncrease < 50 * 1024 * 1024, "内存增长不应该超过50MB");
    }

    @Test
    @DisplayName("性能统计验证")
    void testPerformanceStatsAccuracy() {
        int iterations = 100;

        // 执行一些操作
        for (int i = 0; i < iterations; i++) {
            encryptor.encrypt(MEDIUM_TEXT, EncryptType.AES);
            encryptor.decrypt("dummy", EncryptType.AES); // 这会失败但会被统计
        }

        var stats = EncryptionMonitorManager.getInstance().getPerformanceStats();

        System.out.println("性能统计结果：");
        stats.forEach((key, value) -> System.out.printf("  %s: %d%n", key, value));

        // 验证统计数据
        assertFalse(stats.isEmpty(), "应该有性能统计数据");

        // 检查是否有成功和失败的统计
        boolean hasSuccessStats = stats.keySet().stream().anyMatch(key -> key.contains("success"));
        boolean hasFailureStats = stats.keySet().stream().anyMatch(key -> key.contains("failure"));

        assertTrue(hasSuccessStats, "应该有成功操作的统计");
        assertTrue(hasFailureStats, "应该有失败操作的统计");
    }

    /**
     * 生成指定长度的测试文本
     */
    private static String generateLongText(int length) {
        StringBuilder sb = new StringBuilder();
        String base = "This is a test text for encryption performance testing. ";
        while (sb.length() < length) {
            sb.append(base);
        }
        return sb.substring(0, length);
    }
}
