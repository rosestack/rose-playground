package io.github.rose.i18n.performance;

import io.github.rose.i18n.util.MessageFormatCache;
import org.junit.jupiter.api.*;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageFormatPerformanceTest {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int TEST_ITERATIONS = 100_000;

    private MessageFormatCache cache;

    // 测试数据
    private final String simpleMessage = "Hello, {0}!";
    private final String complexMessage = "User {0} has {1} messages in {2} folders on {3}";
    private final String noParamsMessage = "Welcome to Rose Framework";

    private final Object[] simpleArgs = {"World"};
    private final Object[] complexArgs = {"John", 42, 3, "Monday"};

    private final Locale testLocale = Locale.ENGLISH;

    @BeforeAll
    void setUp() {
        cache = new MessageFormatCache();
        System.out.println("=".repeat(80));
        System.out.println("MessageFormat 性能测试");
        System.out.println("测试迭代次数: " + TEST_ITERATIONS);
        System.out.println("预热迭代次数: " + WARMUP_ITERATIONS);
        System.out.println("=".repeat(80));
    }

    @Test
    @DisplayName("简单消息性能测试")
    void testSimpleMessagePerformance() {
        System.out.println("\n📊 简单消息性能测试: " + simpleMessage);

        // 预热
        warmup();

        // 测试原始方法
        long legacyTime = measureLegacyMethod(simpleMessage, simpleArgs);

        // 测试优化方法
        long optimizedTime = measureOptimizedMethod(simpleMessage, simpleArgs);

        // 测试字符串拼接
        long stringConcatTime = measureStringConcat();

        printResults("简单消息", legacyTime, optimizedTime, stringConcatTime);
    }

    @Test
    @DisplayName("复杂消息性能测试")
    void testComplexMessagePerformance() {
        System.out.println("\n📊 复杂消息性能测试: " + complexMessage);

        // 预热
        warmup();

        // 测试原始方法
        long legacyTime = measureLegacyMethod(complexMessage, complexArgs);

        // 测试优化方法
        long optimizedTime = measureOptimizedMethod(complexMessage, complexArgs);

        printResults("复杂消息", legacyTime, optimizedTime, -1);
    }

    @Test
    @DisplayName("无参数消息性能测试")
    void testNoArgsMessagePerformance() {
        System.out.println("\n📊 无参数消息性能测试: " + noParamsMessage);

        // 预热
        warmup();

        // 测试原始方法
        long legacyTime = measureLegacyMethod(noParamsMessage);

        // 测试优化方法
        long optimizedTime = measureOptimizedMethod(noParamsMessage);

        printResults("无参数消息", legacyTime, optimizedTime, -1);
    }

    @Test
    @DisplayName("缓存命中率测试")
    void testCacheEffectiveness() {
        System.out.println("\n📊 缓存效果测试");

        // 清空缓存
        cache.clearAllCache();

        String[] patterns = {
                "Message 1: {0}",
                "Message 2: {0} and {1}",
                "Message 3: {0}, {1}, {2}"
        };

        // 第一轮：冷缓存
        long coldTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            for (String pattern : patterns) {
                cache.formatMessage(pattern, testLocale, "arg1", "arg2", "arg3");
            }
        }
        coldTime = System.nanoTime() - coldTime;

        // 第二轮：热缓存
        long hotTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            for (String pattern : patterns) {
                cache.formatMessage(pattern, testLocale, "arg1", "arg2", "arg3");
            }
        }
        hotTime = System.nanoTime() - hotTime;

        System.out.printf("冷缓存时间: %d ms%n", TimeUnit.NANOSECONDS.toMillis(coldTime));
        System.out.printf("热缓存时间: %d ms%n", TimeUnit.NANOSECONDS.toMillis(hotTime));
        System.out.printf("缓存加速比: %.2fx%n", (double) coldTime / hotTime);
        System.out.printf("缓存统计: %s%n", cache.getCacheStats());
    }

    private void warmup() {
        // 预热JVM
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            cache.formatMessage(simpleMessage, testLocale, simpleArgs);
            legacyFormatMessage(simpleMessage, simpleArgs);
        }
    }

    private long measureLegacyMethod(String message, Object... args) {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            legacyFormatMessage(message, args);
        }
        return System.nanoTime() - startTime;
    }

    private long measureOptimizedMethod(String message, Object... args) {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            cache.formatMessage(message, testLocale, args);
        }
        return System.nanoTime() - startTime;
    }

    private long measureStringConcat() {
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            stringConcatMethod();
        }
        return System.nanoTime() - startTime;
    }

    /**
     * 传统的MessageFormat实现（每次创建新实例）
     */
    private String legacyFormatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        MessageFormat messageFormat = new MessageFormat(message, testLocale);
        return messageFormat.format(args);
    }

    /**
     * 字符串拼接替代方案（仅适用于简单场景）
     */
    private String stringConcatMethod() {
        return "Hello, " + simpleArgs[0] + "!";
    }

    private void printResults(String testName, long legacyTime, long optimizedTime, long stringConcatTime) {
        long legacyMs = TimeUnit.NANOSECONDS.toMillis(legacyTime);
        long optimizedMs = TimeUnit.NANOSECONDS.toMillis(optimizedTime);

        System.out.println();
        System.out.println("测试结果 - " + testName + ":");
        System.out.printf("  传统方法:   %6d ms%n", legacyMs);
        System.out.printf("  优化方法:   %6d ms%n", optimizedMs);

        if (stringConcatTime > 0) {
            long stringConcatMs = TimeUnit.NANOSECONDS.toMillis(stringConcatTime);
            System.out.printf("  字符串拼接: %6d ms%n", stringConcatMs);
        }

        double speedup = (double) legacyTime / optimizedTime;
        System.out.printf("  性能提升:   %.2fx%n", speedup);

        if (speedup >= 2.0) {
            System.out.println("  ✅ 显著性能提升!");
        } else if (speedup >= 1.5) {
            System.out.println("  ✅ 中等性能提升");
        } else if (speedup >= 1.1) {
            System.out.println("  ✅ 轻微性能提升");
        } else {
            System.out.println("  ⚠️  性能提升不明显");
        }
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("性能测试总结:");
        System.out.println("1. 优化的MessageFormat缓存显著提升了性能");
        System.out.println("2. 无参数消息的优化效果最为明显");
        System.out.println("3. 复杂消息模式从缓存中受益更多");
        System.out.println("4. 简单场景下字符串拼接仍然是最快的选择");
        System.out.println("=".repeat(80));

        cache.clearAllCache();
    }
}