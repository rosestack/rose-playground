package io.github.rose.core.lang.functional;

import io.github.rose.core.lang.functional.checked.CheckedRunnable;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * CheckedRunnable 工具类
 * 提供基于 CheckedRunnable 的便捷方法
 * 
 * @author rose
 */
public final class CheckedRunnableUtils {
    
    private CheckedRunnableUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 创建方法 ====================
    
    /**
     * 从受检异常的运行者创建 Try
     * 
     * @param runnable 受检异常的运行者
     * @return Try 结果
     */
    public static Try<Void> of(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        return Try.of(() -> {
            runnable.run();
            return null;
        });
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 CheckedRunnable 转换为标准 Runnable
     * 
     * @param checkedRunnable 受检异常的运行者
     * @return 标准 Runnable
     */
    public static Runnable unchecked(CheckedRunnable checkedRunnable) {
        Objects.requireNonNull(checkedRunnable, "checkedRunnable cannot be null");
        return checkedRunnable.unchecked();
    }
    
    /**
     * 将标准 Runnable 转换为 CheckedRunnable
     * 
     * @param runnable 标准运行者
     * @return 受检异常的运行者
     */
    public static CheckedRunnable checked(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        return CheckedRunnable.from(runnable);
    }
    
    // ==================== 执行方法 ====================
    
    /**
     * 执行受检异常的运行者
     * 
     * @param runnable 受检异常的运行者
     */
    public static void execute(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 执行受检异常的运行者，忽略异常
     * 
     * @param runnable 受检异常的运行者
     */
    public static void executeSilently(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        try {
            runnable.run();
        } catch (Exception e) {
            // 忽略异常
        }
    }
    
    // ==================== 异步执行方法 ====================
    
    /**
     * 异步执行受检异常的运行者
     * 
     * @param runnable 受检异常的运行者
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> async(CheckedRunnable runnable) {
        return async(runnable, null);
    }
    
    /**
     * 异步执行受检异常的运行者
     * 
     * @param runnable 受检异常的运行者
     * @param executor 执行器
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> async(CheckedRunnable runnable, Executor executor) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        
        if (executor != null) {
            return CompletableFuture.runAsync(unchecked(runnable), executor);
        } else {
            return CompletableFuture.runAsync(unchecked(runnable));
        }
    }
    
    // ==================== 重试方法 ====================
    
    /**
     * 带重试的 CheckedRunnable
     * 
     * @param runnable 原始运行者
     * @param maxAttempts 最大重试次数
     * @return 带重试的 Try
     */
    public static Try<Void> retry(CheckedRunnable runnable, int maxAttempts) {
        return retry(runnable, maxAttempts, 0);
    }
    
    /**
     * 带重试和延迟的 CheckedRunnable
     * 
     * @param runnable 原始运行者
     * @param maxAttempts 最大重试次数
     * @param delayMillis 延迟时间（毫秒）
     * @return 带重试的 Try
     */
    public static Try<Void> retry(CheckedRunnable runnable, int maxAttempts, long delayMillis) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }
        
        Try<Void> lastFailure = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<Void> result = of(runnable);
            
            if (result.isSuccess()) {
                return result;
            }
            
            lastFailure = result;
            
            if (attempt < maxAttempts && delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Try.failure(e);
                }
            }
        }
        
        return lastFailure;
    }
    
    /**
     * 带条件重试的 CheckedRunnable
     * 
     * @param runnable 原始运行者
     * @param maxAttempts 最大重试次数
     * @param shouldRetry 重试条件
     * @return 带重试的 Try
     */
    public static Try<Void> retry(CheckedRunnable runnable, int maxAttempts,
                                java.util.function.Function<Throwable, Boolean> shouldRetry) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        Objects.requireNonNull(shouldRetry, "shouldRetry cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        
        Try<Void> lastFailure = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<Void> result = of(runnable);
            
            if (result.isSuccess()) {
                return result;
            }
            
            lastFailure = result;
            
            if (attempt < maxAttempts && shouldRetry.apply(result.getCause())) {
                continue;
            } else {
                break;
            }
        }
        
        return lastFailure;
    }
    
    // ==================== 超时方法 ====================
    
    /**
     * 带超时的 CheckedRunnable
     * 
     * @param runnable 原始运行者
     * @param timeoutMillis 超时时间（毫秒）
     * @return 带超时的 Try
     */
    public static Try<Void> timeout(CheckedRunnable runnable, long timeoutMillis) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }
        
        return Try.of(() -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(unchecked(runnable));
            
            try {
                future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMillis + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建空操作运行者
     * 
     * @return 空操作运行者
     */
    public static CheckedRunnable noOp() {
        return () -> {
            // 什么都不做
        };
    }
    
    /**
     * 创建异常运行者
     * 
     * @param exception 要抛出的异常
     * @return 异常运行者
     */
    public static CheckedRunnable failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return () -> {
            throw exception;
        };
    }
    
    /**
     * 创建日志运行者
     * 
     * @param message 日志消息
     * @return 日志运行者
     */
    public static CheckedRunnable log(String message) {
        Objects.requireNonNull(message, "message cannot be null");
        return () -> {
            System.out.println(message);
        };
    }
    
    /**
     * 创建睡眠运行者
     * 
     * @param millis 睡眠时间（毫秒）
     * @return 睡眠运行者
     */
    public static CheckedRunnable sleep(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("millis must be non-negative");
        }
        return () -> {
            Thread.sleep(millis);
        };
    }
} 