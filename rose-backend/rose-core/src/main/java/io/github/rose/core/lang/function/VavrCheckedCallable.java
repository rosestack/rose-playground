package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Vavr CheckedCallable 工具类
 * 提供对 java.util.concurrent.Callable 的扩展，支持异常安全的操作
 * 
 * @author rose
 */
public class VavrCheckedCallable {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedCallable.class);
    
    /**
     * 从 Callable 创建 Try
     * 
     * @param callable 可调用任务
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> ofCallable(Callable<T> callable) {
        return Try.of(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 从可能抛出异常的 Supplier 创建 Try
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> ofSupplier(Supplier<T> supplier) {
        return Try.of(supplier::get);
    }
    
    /**
     * 从可能抛出异常的函数创建 Try
     * 
     * @param function 可能抛出异常的函数
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> ofFunction(Function0<T> function) {
        return Try.of(() -> {
            try {
                return function.apply();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 执行 Callable，带异常恢复
     * 
     * @param callable 可调用任务
     * @param fallback 异常恢复函数
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> ofCallable(Callable<T> callable, Function1<Throwable, Try<T>> fallback) {
        return ofCallable(callable).recoverWith(fallback);
    }
    
    /**
     * 执行 Callable，带默认值
     * 
     * @param callable 可调用任务
     * @param defaultValue 默认值
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> ofCallable(Callable<T> callable, T defaultValue) {
        return ofCallable(callable).recover(throwable -> defaultValue);
    }
    
    /**
     * 条件执行 Callable
     * 
     * @param condition 条件
     * @param callable 可调用任务
     * @param defaultValue 默认值
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> conditionalCallable(Supplier<Boolean> condition, 
                                                Callable<T> callable, 
                                                T defaultValue) {
        if (condition.get()) {
            return ofCallable(callable);
        }
        return Try.success(defaultValue);
    }
    
    /**
     * 带重试的 Callable
     * 
     * @param callable 可调用任务
     * @param maxRetries 最大重试次数
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> retryCallable(Callable<T> callable, int maxRetries) {
        return Try.of(() -> {
            Try<T> result = ofCallable(callable);
            int retries = 0;
            
            while (result.isFailure() && retries < maxRetries) {
                log.warn("Callable failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
                result = ofCallable(callable);
                retries++;
            }
            
            if (result.isFailure()) {
                throw result.getCause();
            }
            
            return result.get();
        });
    }
    
    /**
     * 带超时的 Callable
     * 
     * @param callable 可调用任务
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> timeoutCallable(Callable<T> callable, long timeoutMs) {
        return Try.of(() -> {
            long startTime = System.currentTimeMillis();
            Try<T> result = ofCallable(callable);
            
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            }
            
            if (result.isFailure()) {
                throw result.getCause();
            }
            
            return result.get();
        });
    }
    
    /**
     * 批量执行 Callable
     * 
     * @param callables 可调用任务列表
     * @param <T> 返回类型
     * @return Try 结果，任一失败则整体失败
     */
    @SafeVarargs
    public static <T> Try<List<T>> executeAllCallables(Callable<T>... callables) {
        return Try.of(() -> {
            List<T> results = List.empty();
            for (Callable<T> callable : callables) {
                results = results.append(callable.call());
            }
            return results;
        });
    }
    
    /**
     * 批量执行 Callable，收集所有结果
     * 
     * @param callables 可调用任务列表
     * @param <T> 返回类型
     * @return 所有结果的列表
     */
    @SafeVarargs
    public static <T> List<Try<T>> executeAllCallablesCollect(Callable<T>... callables) {
        return List.of(callables).map(VavrCheckedCallable::ofCallable);
    }
    
    /**
     * 带日志的 Callable
     * 
     * @param callable 可调用任务
     * @param operationName 操作名称
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> loggedCallable(Callable<T> callable, String operationName) {
        log.debug("Starting operation: {}", operationName);
        
        Try<T> result = ofCallable(callable);
        
        if (result.isSuccess()) {
            log.debug("Operation {} completed successfully with result: {}", operationName, result.get());
        } else {
            log.error("Operation {} failed: {}", operationName, result.getCause().getMessage());
        }
        
        return result;
    }
    
    /**
     * 将标准 Callable 转换为 Vavr Function0
     * 
     * @param callable 标准可调用任务
     * @param <T> 返回类型
     * @return Vavr Function0
     */
    public static <T> Function0<T> toFunction0(Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 将标准 Callable 转换为 Vavr Function0<Try<T>>
     * 
     * @param callable 标准可调用任务
     * @param <T> 返回类型
     * @return Vavr Function0<Try<T>>
     */
    public static <T> Function0<Try<T>> toFunction0Try(Callable<T> callable) {
        return () -> ofCallable(callable);
    }
    
    /**
     * 创建带缓存的 Callable
     * 
     * @param callable 原始可调用任务
     * @param cache 缓存对象
     * @param key 缓存键
     * @param <T> 返回类型
     * @return 带缓存的 Callable
     */
    public static <T> Callable<T> withCache(Callable<T> callable, java.util.Map<String, T> cache, String key) {
        return () -> {
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            
            T result = callable.call();
            cache.put(key, result);
            return result;
        };
    }
    
    /**
     * 创建带验证的 Callable
     * 
     * @param callable 原始可调用任务
     * @param validator 验证器
     * @param <T> 返回类型
     * @return 带验证的 Callable
     */
    public static <T> Callable<T> withValidation(Callable<T> callable, Function1<T, Boolean> validator) {
        return () -> {
            T result = callable.call();
            if (!validator.apply(result)) {
                throw new IllegalArgumentException("Validation failed for result: " + result);
            }
            return result;
        };
    }
    
    /**
     * 创建带异常处理的 Callable
     * 
     * @param callable 原始可调用任务
     * @param exceptionHandler 异常处理函数
     * @param <T> 返回类型
     * @return 带异常处理的 Callable
     */
    public static <T> Callable<T> withExceptionHandling(Callable<T> callable, 
                                                       Function1<Throwable, T> exceptionHandler) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }
    
    /**
     * 创建带降级的 Callable
     * 
     * @param primary 主要可调用任务
     * @param fallback 降级可调用任务
     * @param <T> 返回类型
     * @return 带降级的 Callable
     */
    public static <T> Callable<T> withFallback(Callable<T> primary, Callable<T> fallback) {
        return () -> {
            try {
                return primary.call();
            } catch (Exception e) {
                log.warn("Primary callable failed, using fallback: {}", e.getMessage());
                return fallback.call();
            }
        };
    }
    
    /**
     * 创建带指标的 Callable
     * 
     * @param callable 原始可调用任务
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 返回类型
     * @return 带指标的 Callable
     */
    public static <T> Callable<T> withMetrics(Callable<T> callable,
                                             Function1<Long, Void> metricsCollector,
                                             String operationName) {
        return () -> {
            long startTime = System.currentTimeMillis();
            try {
                T result = callable.call();
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration);
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration);
                throw e;
            }
        };
    }
} 