package io.github.rose.core.lang.functional;

import io.github.rose.core.lang.functional.checked.CheckedCallable;
import io.vavr.control.Try;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * CheckedCallable 工具类
 * 提供基于 CheckedCallable 的便捷方法
 * 
 * @author rose
 */
public final class CheckedCallableUtils {
    
    private CheckedCallableUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 创建方法 ====================
    
    /**
     * 从受检异常的可调用者创建 Try
     * 
     * @param callable 受检异常的可调用者
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> of(CheckedCallable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        return Try.of(callable::call);
    }
    
    /**
     * 从受检异常的可调用者创建 Option
     * 
     * @param callable 受检异常的可调用者
     * @param <T> 返回类型
     * @return Option 结果
     */
    public static <T> Option<T> ofOption(CheckedCallable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        try {
            T result = callable.call();
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            return Option.none();
        }
    }
    
    /**
     * 从受检异常的可调用者创建 Option（保留异常信息）
     * 
     * @param callable 受检异常的可调用者
     * @param <T> 返回类型
     * @return Option 结果
     */
    public static <T> Option<T> ofOptionWithException(CheckedCallable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        try {
            T result = callable.call();
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            // 记录异常但不抛出
            e.printStackTrace();
            return Option.none();
        }
    }
    
    /**
     * 从受检异常的可调用者创建 Option（带异常处理）
     * 
     * @param callable 受检异常的可调用者
     * @param exceptionHandler 异常处理函数
     * @param <T> 返回类型
     * @return Option 结果
     */
    public static <T> Option<T> ofOption(CheckedCallable<T> callable,
                                       java.util.function.Function<Exception, T> exceptionHandler) {
        Objects.requireNonNull(callable, "callable cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        try {
            T result = callable.call();
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            T fallback = exceptionHandler.apply(e);
            return fallback != null ? Option.some(fallback) : Option.none();
        }
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 CheckedCallable 转换为标准 Callable
     * 
     * @param checkedCallable 受检异常的可调用者
     * @param <T> 返回类型
     * @return 标准 Callable
     */
    public static <T> Callable<T> unchecked(CheckedCallable<T> checkedCallable) {
        Objects.requireNonNull(checkedCallable, "checkedCallable cannot be null");
        return checkedCallable.unchecked();
    }
    
    /**
     * 将标准 Callable 转换为 CheckedCallable
     * 
     * @param callable 标准可调用者
     * @param <T> 返回类型
     * @return 受检异常的可调用者
     */
    public static <T> CheckedCallable<T> checked(Callable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        return CheckedCallable.from(callable);
    }
    
    // ==================== 执行方法 ====================
    
    /**
     * 执行受检异常的可调用者
     * 
     * @param callable 受检异常的可调用者
     * @param <T> 返回类型
     * @return 执行结果
     */
    public static <T> T execute(CheckedCallable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 执行受检异常的可调用者，忽略异常
     * 
     * @param callable 受检异常的可调用者
     * @param defaultValue 默认值
     * @param <T> 返回类型
     * @return 执行结果或默认值
     */
    public static <T> T executeSilently(CheckedCallable<T> callable, T defaultValue) {
        Objects.requireNonNull(callable, "callable cannot be null");
        try {
            T result = callable.call();
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    // ==================== 异步执行方法 ====================
    
    /**
     * 异步执行受检异常的可调用者
     * 
     * @param callable 受检异常的可调用者
     * @param <T> 返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> async(CheckedCallable<T> callable) {
        return async(callable, null);
    }
    
    /**
     * 异步执行受检异常的可调用者
     * 
     * @param callable 受检异常的可调用者
     * @param executor 执行器
     * @param <T> 返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> async(CheckedCallable<T> callable, Executor executor) {
        Objects.requireNonNull(callable, "callable cannot be null");
        
        if (executor != null) {
            return CompletableFuture.supplyAsync(() -> execute(callable), executor);
        } else {
            return CompletableFuture.supplyAsync(() -> execute(callable));
        }
    }
    
    // ==================== 重试方法 ====================
    
    /**
     * 带重试的 CheckedCallable
     * 
     * @param callable 原始可调用者
     * @param maxAttempts 最大重试次数
     * @param <T> 返回类型
     * @return 带重试的 Try
     */
    public static <T> Try<T> retry(CheckedCallable<T> callable, int maxAttempts) {
        return retry(callable, maxAttempts, 0);
    }
    
    /**
     * 带重试和延迟的 CheckedCallable
     * 
     * @param callable 原始可调用者
     * @param maxAttempts 最大重试次数
     * @param delayMillis 延迟时间（毫秒）
     * @param <T> 返回类型
     * @return 带重试的 Try
     */
    public static <T> Try<T> retry(CheckedCallable<T> callable, int maxAttempts, long delayMillis) {
        Objects.requireNonNull(callable, "callable cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }
        
        Try<T> lastFailure = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<T> result = of(callable);
            
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
     * 带条件重试的 CheckedCallable
     * 
     * @param callable 原始可调用者
     * @param maxAttempts 最大重试次数
     * @param shouldRetry 重试条件
     * @param <T> 返回类型
     * @return 带重试的 Try
     */
    public static <T> Try<T> retry(CheckedCallable<T> callable, int maxAttempts,
                                 java.util.function.Function<Throwable, Boolean> shouldRetry) {
        Objects.requireNonNull(callable, "callable cannot be null");
        Objects.requireNonNull(shouldRetry, "shouldRetry cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        
        Try<T> lastFailure = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<T> result = of(callable);
            
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
     * 带超时的 CheckedCallable
     * 
     * @param callable 原始可调用者
     * @param timeoutMillis 超时时间（毫秒）
     * @param <T> 返回类型
     * @return 带超时的 Try
     */
    public static <T> Try<T> timeout(CheckedCallable<T> callable, long timeoutMillis) {
        Objects.requireNonNull(callable, "callable cannot be null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }
        
        return Try.of(() -> {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> execute(callable));
            
            try {
                return future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMillis + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建常量可调用者
     * 
     * @param value 常量值
     * @param <T> 值类型
     * @return 常量可调用者
     */
    public static <T> CheckedCallable<T> constant(T value) {
        return CheckedCallable.constant(value);
    }
    
    /**
     * 创建空值可调用者
     * 
     * @param <T> 值类型
     * @return 空值可调用者
     */
    public static <T> CheckedCallable<T> empty() {
        return () -> null;
    }
    
    /**
     * 创建异常可调用者
     * 
     * @param exception 要抛出的异常
     * @param <T> 值类型
     * @return 异常可调用者
     */
    public static <T> CheckedCallable<T> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return () -> {
            throw exception;
        };
    }
} 