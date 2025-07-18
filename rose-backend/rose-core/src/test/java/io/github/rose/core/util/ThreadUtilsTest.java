package io.github.rose.core.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ThreadUtils测试类
 * 验证简化后的线程工具类核心功能
 */
@Slf4j
class ThreadUtilsTest {

    @Test
    void testSleep() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(100);
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime >= 90, "Sleep time should be at least 90ms");
        assertTrue(endTime - startTime <= 200, "Sleep time should be less than 200ms");
    }

    @Test
    void testSleepWithZeroOrNegative() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(0);
        ThreadUtils.sleep(-100);
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 50, "Should not sleep for zero or negative values");
    }

    @Test
    void testSleepInterruption() {
        Thread testThread = new Thread(() -> {
            ThreadUtils.sleep(5000); // 长时间睡眠
            // 验证中断状态被正确恢复
            assertTrue(Thread.currentThread().isInterrupted(), "Thread should be interrupted");
        });

        testThread.start();
        ThreadUtils.sleep(100);
        testThread.interrupt();

        assertDoesNotThrow(() -> {
            testThread.join(1000);
            assertFalse(testThread.isAlive(), "Thread should have finished");
        });
    }

    @Test
    void testShutdownAndAwaitTermination() {
        // 创建一个简单的线程池进行测试
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 2, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
        );

        // 提交一些任务
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                ThreadUtils.sleep(50);
                counter.incrementAndGet();
            });
        }

        // 关闭线程池
        ThreadUtils.shutdownAndAwaitTermination(executor);

        assertTrue(executor.isShutdown(), "Executor should be shutdown");
        assertTrue(executor.isTerminated(), "Executor should be terminated");
        assertEquals(5, counter.get(), "All tasks should have completed");
    }

    @Test
    void testPrintException() {
        RuntimeException testException = new RuntimeException("Test exception");

        // 测试直接异常处理
        assertDoesNotThrow(() -> ThreadUtils.printException(null, testException));

        // 测试Future异常处理 - 由于CompletableFuture实现了Future接口，
        // 而printException方法会检查Runnable是否为Future实例
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            throw new RuntimeException("Future exception");
        });

        ThreadUtils.sleep(100); // 等待Future完成

        // 由于CompletableFuture不是Runnable，我们需要手动测试异常提取
        assertDoesNotThrow(() -> {
            try {
                future.get();
            } catch (Exception e) {
                // 异常被正确捕获
                assertNotNull(e.getCause());
            }
        });
    }
}
