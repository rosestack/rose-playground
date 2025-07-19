package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function1;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Vavr Function 工具类
 * 提供对 Vavr Function1 和标准 Function 的工具方法和转换功能
 * 
 * @author rose
 */
public class VavrCheckedFunction {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedFunction.class);
    
    // ==================== 转换方法 ====================
    
    /**
     * 将标准 Function 转换为 Vavr Function1
     * 
     * @param function 标准函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Vavr Function1
     */
    public static <T, R> Function1<T, R> toFunction1(Function<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return function::apply;
    }
    
    /**
     * 将标准 Function 转换为 Vavr Function1<Try<R>>
     * 
     * @param function 标准函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Vavr Function1<Try<R>>
     */
    public static <T, R> Function1<T, Try<R>> toFunction1Try(Function<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return value -> Try.of(() -> function.apply(value));
    }
    
    /**
     * 将 Vavr Function1 转换为标准 Function
     * 
     * @param function Vavr 函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 标准 Function
     */
    public static <T, R> Function<T, R> toFunction(Function1<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return function::apply;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 执行 Function 并返回 Try
     * 
     * @param function 函数
     * @param value 输入值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, R> Try<R> execute(Function<T, R> function, T value) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> function.apply(value));
    }
    
    /**
     * 执行 Vavr Function1 并返回 Try
     * 
     * @param function Vavr 函数
     * @param value 输入值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, R> Try<R> execute(Function1<T, R> function, T value) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> function.apply(value));
    }
    
    // ==================== 装饰器方法 ====================
    
    /**
     * 创建带重试的 Function
     * 
     * @param function 原始函数
     * @param maxRetries 最大重试次数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 带重试的 Function
     */
    public static <T, R> Function<T, R> withRetry(Function<T, R> function, int maxRetries) {
        Objects.requireNonNull(function, "function cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        
        return value -> {
            Try<R> result = Try.of(() -> function.apply(value));
            int retries = 0;
            
            while (result.isFailure() && retries < maxRetries) {
                log.warn("Function failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
                result = Try.of(() -> function.apply(value));
                retries++;
            }
            
            if (result.isSuccess()) {
                return result.get();
            } else {
                throw new RuntimeException(result.getCause());
            }
        };
    }
    
    /**
     * 创建带超时的 Function
     * 
     * @param function 原始函数
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 带超时的 Function
     */
    public static <T, R> Function<T, R> withTimeout(Function<T, R> function, long timeoutMs) {
        Objects.requireNonNull(function, "function cannot be null");
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        
        return value -> {
            try {
                CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> function.apply(value));
                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 创建带日志的 Function
     * 
     * @param function 原始函数
     * @param operationName 操作名称
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 带日志的 Function
     */
    public static <T, R> Function<T, R> withLogging(Function<T, R> function, String operationName) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return value -> {
            log.debug("Starting function operation: {} with value: {}", operationName, value);
            
            try {
                R result = function.apply(value);
                log.debug("Function operation {} completed successfully with result: {}", operationName, result);
                return result;
            } catch (Exception e) {
                log.error("Function operation {} failed: {}", operationName, e.getMessage());
                throw e;
            }
        };
    }
    
    /**
     * 创建带降级的 Function
     * 
     * @param primary 主要函数
     * @param fallback 降级函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 带降级的 Function
     */
    public static <T, R> Function<T, R> withFallback(Function<T, R> primary, Function<T, R> fallback) {
        Objects.requireNonNull(primary, "primary function cannot be null");
        Objects.requireNonNull(fallback, "fallback function cannot be null");
        
        return value -> {
            try {
                return primary.apply(value);
            } catch (Exception e) {
                log.warn("Primary function failed, using fallback: {}", e.getMessage());
                return fallback.apply(value);
            }
        };
    }
    
    /**
     * 创建带验证的 Function
     * 
     * @param function 原始函数
     * @param validator 验证器
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 带验证的 Function
     */
    public static <T, R> Function<T, R> withValidation(Function<T, R> function, Function<R, Boolean> validator) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");
        
        return value -> {
            R result = function.apply(value);
            if (!validator.apply(result)) {
                throw new IllegalArgumentException("Validation failed for result: " + result);
            }
            return result;
        };
    }
    
    /**
     * 创建带缓存的 Function
     * 
     * @param function 原始函数
     * @param cache 缓存对象
     * @param keyGenerator 缓存键生成器
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 带缓存的 Function
     */
    public static <T, R> Function<T, R> withCache(Function<T, R> function,
                                                 java.util.Map<String, R> cache,
                                                 Function<T, String> keyGenerator) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(cache, "cache cannot be null");
        Objects.requireNonNull(keyGenerator, "keyGenerator cannot be null");
        
        return value -> {
            String cacheKey = keyGenerator.apply(value);
            if (cache.containsKey(cacheKey)) {
                return cache.get(cacheKey);
            }
            
            R result = function.apply(value);
            cache.put(cacheKey, result);
            return result;
        };
    }
    
    // ==================== 组合方法 ====================
    
    /**
     * 条件执行 Function
     * 
     * @param value 输入值
     * @param condition 条件
     * @param function 函数
     * @param defaultValue 默认值
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 结果
     */
    public static <T, R> R conditional(T value, Function<T, Boolean> condition,
                                     Function<T, R> function, R defaultValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        if (condition.apply(value)) {
            return function.apply(value);
        }
        return defaultValue;
    }
    
    /**
     * 批量执行 Function
     * 
     * @param items 输入项列表
     * @param function 函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 结果列表
     */
    public static <T, R> List<R> forEach(Iterable<T> items, Function<T, R> function) {
        Objects.requireNonNull(items, "items cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return List.ofAll(items).map(function::apply);
    }
    
    /**
     * 批量执行 Function，收集所有结果（包括失败）
     * 
     * @param items 输入项列表
     * @param function 函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Try 结果列表
     */
    public static <T, R> List<Try<R>> forEachCollect(Iterable<T> items, Function<T, R> function) {
        Objects.requireNonNull(items, "items cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return List.ofAll(items).map(item -> Try.of(() -> function.apply(item)));
    }
    
    /**
     * 链式执行多个 Function
     * 
     * @param value 输入值
     * @param functions 函数列表
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 最终结果
     */
    @SafeVarargs
    public static <T, R> R chain(T value, Function<T, R>... functions) {
        Objects.requireNonNull(functions, "functions cannot be null");
        if (functions.length == 0) {
            throw new IllegalArgumentException("functions array cannot be empty");
        }
        
        R result = null;
        for (Function<T, R> function : functions) {
            Objects.requireNonNull(function, "function in array cannot be null");
            result = function.apply(value);
        }
        return result;
    }
    
    /**
     * 创建带异步的 Function
     * 
     * @param function 原始函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 异步 Function
     */
    public static <T, R> Function<T, CompletableFuture<R>> withAsync(Function<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        
        return value -> CompletableFuture.supplyAsync(() -> function.apply(value));
    }
    
    /**
     * 创建带并行处理的 Function
     * 
     * @param functions 函数列表
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return 并行处理 Function
     */
    @SafeVarargs
    public static <T, R> Function<T, java.util.List<R>> withParallel(Function<T, R>... functions) {
        Objects.requireNonNull(functions, "functions cannot be null");
        if (functions.length == 0) {
            throw new IllegalArgumentException("functions array cannot be empty");
        }
        
        return value -> {
            try {
                return java.util.Arrays.stream(functions)
                        .parallel()
                        .map(f -> f.apply(value))
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Parallel execution failed", e);
            }
        };
    }
} 