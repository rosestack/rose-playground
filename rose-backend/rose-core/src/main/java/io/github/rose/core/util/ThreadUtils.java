package io.github.rose.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 线程相关工具类
 * 提供基础的线程操作工具方法
 *
 * @author zhijun.chen
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadUtils {

    /**
     * 线程睡眠等待（毫秒）
     * 改进异常处理，恢复中断状态
     *
     * @param milliseconds 睡眠时间（毫秒）
     */
    public static void sleep(long milliseconds) {
        if (milliseconds <= 0) {
            return;
        }
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.debug("Thread sleep interrupted");
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 优雅关闭线程池
     * 先使用shutdown停止接收新任务并尝试完成所有已存在任务
     * 如果超时，则调用shutdownNow取消在workQueue中Pending的任务并中断所有阻塞函数
     * 如果仍然超时，则强制退出
     * 另对在shutdown时线程本身被调用中断做了处理
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        if (pool != null && !pool.isShutdown()) {
            log.info("关闭后台任务线程池 {}", pool.getClass().getSimpleName());
            pool.shutdown();
            try {
                if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        log.info("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 打印线程异常信息
     */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            log.error(t.getMessage(), t);
        }
    }
}
