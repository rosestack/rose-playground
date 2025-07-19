package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

/**
 * Vavr CheckedBiFunction 工具类
 * 提供对 java.util.function.BiFunction 的扩展，支持异常安全的操作
 * 
 * @author rose
 */
public class VavrCheckedBiFunction {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedBiFunction.class);
    
    /**
     * 从 BiFunction 创建 Try
     * 
     * @param function 二元函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> ofBiFunction(BiFunction<T, U, R> function, T t, U u) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> function.apply(t, u));
    }
    
    /**
     * 从可能抛出异常的函数创建 Try
     * 
     * @param function 可能抛出异常的函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> ofFunction(Function2<T, U, R> function, T t, U u) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> function.apply(t, u));
    }
    
    /**
     * 执行 BiFunction，带异常恢复
     * 
     * @param function 二元函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param fallback 异常恢复函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> ofBiFunction(BiFunction<T, U, R> function, T t, U u, 
                                               Function2<Throwable, Throwable, Try<R>> fallback) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(fallback, "fallback cannot be null");
        return ofBiFunction(function, t, u).recoverWith(throwable -> fallback.apply(throwable, throwable));
    }
    
    /**
     * 执行 BiFunction，带默认值
     * 
     * @param function 二元函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param defaultValue 默认值
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> ofBiFunction(BiFunction<T, U, R> function, T t, U u, R defaultValue) {
        Objects.requireNonNull(function, "function cannot be null");
        return ofBiFunction(function, t, u).recover(throwable -> defaultValue);
    }
    
    /**
     * 条件执行 BiFunction
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param condition 条件
     * @param function 二元函数
     * @param defaultValue 默认值
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> conditionalBiFunction(T t, U u, Function2<T, U, Boolean> condition,
                                                        BiFunction<T, U, R> function, R defaultValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        if (condition.apply(t, u)) {
            return ofBiFunction(function, t, u);
        }
        return Try.success(defaultValue);
    }
    
    /**
     * 带重试的 BiFunction
     * 
     * @param function 二元函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param maxRetries 最大重试次数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> retryBiFunction(BiFunction<T, U, R> function, T t, U u, int maxRetries) {
        Objects.requireNonNull(function, "function cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        
        Try<R> result = ofBiFunction(function, t, u);
        int retries = 0;
        
        while (result.isFailure() && retries < maxRetries) {
            log.warn("BiFunction failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
            result = ofBiFunction(function, t, u);
            retries++;
        }
        
        return result;
    }
    
    /**
     * 带超时的 BiFunction
     * 
     * @param function 二元函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> timeoutBiFunction(BiFunction<T, U, R> function, T t, U u, long timeoutMs) {
        Objects.requireNonNull(function, "function cannot be null");
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        
        return Try.of(() -> {
            CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> function.apply(t, u));
            try {
                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            }
        });
    }
    
    /**
     * 带日志的 BiFunction
     * 
     * @param function 二元函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> loggedBiFunction(BiFunction<T, U, R> function, T t, U u, String operationName) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        log.debug("Starting BiFunction operation: {} with values: {}, {}", operationName, t, u);
        
        Try<R> result = ofBiFunction(function, t, u);
        
        if (result.isSuccess()) {
            log.debug("BiFunction operation {} completed successfully with result: {}", operationName, result.get());
        } else {
            log.error("BiFunction operation {} failed: {}", operationName, result.getCause().getMessage());
        }
        
        return result;
    }
    
    /**
     * 将标准 BiFunction 转换为 Vavr Function2
     * 
     * @param function 标准二元函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Vavr Function2
     */
    public static <T, U, R> Function2<T, U, R> toFunction2(BiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return function::apply;
    }
    
    /**
     * 将标准 BiFunction 转换为 Vavr Function2<Try<R>>
     * 
     * @param function 标准二元函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Vavr Function2<Try<R>>
     */
    public static <T, U, R> Function2<T, U, Try<R>> toFunction2Try(BiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return (t, u) -> ofBiFunction(function, t, u);
    }
    
    /**
     * 创建带验证的 BiFunction
     * 
     * @param function 原始二元函数
     * @param validator 验证器
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带验证的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withValidation(BiFunction<T, U, R> function, 
                                                              Function2<R, R, Boolean> validator) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");
        
        return (t, u) -> {
            R result = function.apply(t, u);
            if (!validator.apply(result, result)) {
                throw new IllegalArgumentException("Validation failed for result: " + result);
            }
            return result;
        };
    }
    
    /**
     * 创建带异常处理的 BiFunction
     * 
     * @param function 原始二元函数
     * @param exceptionHandler 异常处理函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带异常处理的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withExceptionHandling(BiFunction<T, U, R> function, 
                                                                     Function2<Throwable, Throwable, R> exceptionHandler) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        
        return (t, u) -> {
            try {
                return function.apply(t, u);
            } catch (Exception e) {
                return exceptionHandler.apply(e, e);
            }
        };
    }
    
    /**
     * 创建带降级的 BiFunction
     * 
     * @param primary 主要二元函数
     * @param fallback 降级二元函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带降级的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withFallback(BiFunction<T, U, R> primary, 
                                                            BiFunction<T, U, R> fallback) {
        Objects.requireNonNull(primary, "primary function cannot be null");
        Objects.requireNonNull(fallback, "fallback function cannot be null");
        
        return (t, u) -> {
            try {
                return primary.apply(t, u);
            } catch (Exception e) {
                log.warn("Primary BiFunction failed, using fallback: {}", e.getMessage());
                return fallback.apply(t, u);
            }
        };
    }
    
    /**
     * 创建带指标的 BiFunction
     * 
     * @param function 原始二元函数
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带指标的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withMetrics(BiFunction<T, U, R> function,
                                                           Function2<Long, Long, Void> metricsCollector,
                                                           String operationName) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(metricsCollector, "metricsCollector cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return (t, u) -> {
            long startTime = System.currentTimeMillis();
            try {
                R result = function.apply(t, u);
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration, duration);
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration, duration);
                throw e;
            }
        };
    }
    
    /**
     * 创建带重试的 BiFunction
     * 
     * @param function 原始二元函数
     * @param maxRetries 最大重试次数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带重试的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withRetry(BiFunction<T, U, R> function, int maxRetries) {
        Objects.requireNonNull(function, "function cannot be null");
        return (t, u) -> retryBiFunction(function, t, u, maxRetries).get();
    }
    
    /**
     * 创建带超时的 BiFunction
     * 
     * @param function 原始二元函数
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带超时的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withTimeout(BiFunction<T, U, R> function, long timeoutMs) {
        Objects.requireNonNull(function, "function cannot be null");
        return (t, u) -> timeoutBiFunction(function, t, u, timeoutMs).get();
    }
    
    /**
     * 创建带日志的 BiFunction
     * 
     * @param function 原始二元函数
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带日志的 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> withLogging(BiFunction<T, U, R> function, String operationName) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return (t, u) -> loggedBiFunction(function, t, u, operationName).get();
    }
} 