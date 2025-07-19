package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Vavr Supplier 工具类
 * 提供对 Vavr Function0 和标准 Supplier 的工具方法和转换功能
 * 
 * @author rose
 */
public class VavrCheckedSupplier {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedSupplier.class);
    
    // ==================== 转换方法 ====================
    
    /**
     * 将标准 Supplier 转换为 Vavr Function0
     * 
     * @param supplier 标准供应者
     * @param <T> 返回类型
     * @return Vavr Function0
     */
    public static <T> Function0<T> toFunction0(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return supplier::get;
    }
    
    /**
     * 将标准 Supplier 转换为 Vavr Function0<Try<T>>
     * 
     * @param supplier 标准供应者
     * @param <T> 返回类型
     * @return Vavr Function0<Try<T>>
     */
    public static <T> Function0<Try<T>> toFunction0Try(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return () -> Try.of(supplier::get);
    }
    
    /**
     * 将 Vavr Function0 转换为标准 Supplier
     * 
     * @param function Vavr 函数
     * @param <T> 返回类型
     * @return 标准 Supplier
     */
    public static <T> Supplier<T> toSupplier(Function0<T> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return function::apply;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 执行 Supplier 并返回 Try
     * 
     * @param supplier 供应者
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> execute(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return Try.of(supplier::get);
    }
    
    /**
     * 执行 Vavr Function0 并返回 Try
     * 
     * @param function Vavr 函数
     * @param <T> 返回类型
     * @return Try 结果
     */
    public static <T> Try<T> execute(Function0<T> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> function.apply());
    }
    
    // ==================== 装饰器方法 ====================
    
    /**
     * 创建带重试的 Supplier
     * 
     * @param supplier 原始供应者
     * @param maxRetries 最大重试次数
     * @param <T> 返回类型
     * @return 带重试的 Supplier
     */
    public static <T> Supplier<T> withRetry(Supplier<T> supplier, int maxRetries) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        
        return () -> {
            Try<T> result = Try.of(supplier::get);
            int retries = 0;
            
            while (result.isFailure() && retries < maxRetries) {
                log.warn("Supplier failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
                result = Try.of(supplier::get);
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
     * 创建带超时的 Supplier
     * 
     * @param supplier 原始供应者
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 返回类型
     * @return 带超时的 Supplier
     */
    public static <T> Supplier<T> withTimeout(Supplier<T> supplier, long timeoutMs) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        
        return () -> {
            try {
                CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier);
                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 创建带日志的 Supplier
     * 
     * @param supplier 原始供应者
     * @param operationName 操作名称
     * @param <T> 返回类型
     * @return 带日志的 Supplier
     */
    public static <T> Supplier<T> withLogging(Supplier<T> supplier, String operationName) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return () -> {
            log.debug("Starting supplier operation: {}", operationName);
            
            try {
                T result = supplier.get();
                log.debug("Supplier operation {} completed successfully with result: {}", operationName, result);
                return result;
            } catch (Exception e) {
                log.error("Supplier operation {} failed: {}", operationName, e.getMessage());
                throw e;
            }
        };
    }
    
    /**
     * 创建带降级的 Supplier
     * 
     * @param primary 主要供应者
     * @param fallback 降级供应者
     * @param <T> 返回类型
     * @return 带降级的 Supplier
     */
    public static <T> Supplier<T> withFallback(Supplier<T> primary, Supplier<T> fallback) {
        Objects.requireNonNull(primary, "primary supplier cannot be null");
        Objects.requireNonNull(fallback, "fallback supplier cannot be null");
        
        return () -> {
            try {
                return primary.get();
            } catch (Exception e) {
                log.warn("Primary supplier failed, using fallback: {}", e.getMessage());
                return fallback.get();
            }
        };
    }
    
    /**
     * 创建带缓存的 Supplier
     * 
     * @param supplier 原始供应者
     * @param cache 缓存对象
     * @param key 缓存键
     * @param <T> 返回类型
     * @return 带缓存的 Supplier
     */
    public static <T> Supplier<T> withCache(Supplier<T> supplier, java.util.Map<String, T> cache, String key) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(cache, "cache cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        
        return () -> {
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            
            T result = supplier.get();
            cache.put(key, result);
            return result;
        };
    }
    
    /**
     * 创建带验证的 Supplier
     * 
     * @param supplier 原始供应者
     * @param validator 验证器
     * @param <T> 返回类型
     * @return 带验证的 Supplier
     */
    public static <T> Supplier<T> withValidation(Supplier<T> supplier, Function1<T, Boolean> validator) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");
        
        return () -> {
            T result = supplier.get();
            if (!validator.apply(result)) {
                throw new IllegalArgumentException("Validation failed for result: " + result);
            }
            return result;
        };
    }
    
    /**
     * 创建带异常处理的 Supplier
     * 
     * @param supplier 原始供应者
     * @param exceptionHandler 异常处理器
     * @param <T> 返回类型
     * @return 带异常处理的 Supplier
     */
    public static <T> Supplier<T> withExceptionHandling(Supplier<T> supplier, 
                                                       Function1<Throwable, T> exceptionHandler) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }
    
    /**
     * 创建带指标的 Supplier
     * 
     * @param supplier 原始供应者
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 返回类型
     * @return 带指标的 Supplier
     */
    public static <T> Supplier<T> withMetrics(Supplier<T> supplier,
                                             Function1<Long, Void> metricsCollector,
                                             String operationName) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(metricsCollector, "metricsCollector cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return () -> {
            long startTime = System.currentTimeMillis();
            try {
                T result = supplier.get();
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
    
    // ==================== 组合方法 ====================
    
    /**
     * 条件执行 Supplier
     * 
     * @param condition 条件
     * @param supplier 供应者
     * @param defaultValue 默认值
     * @param <T> 返回类型
     * @return 结果
     */
    public static <T> T conditional(Supplier<Boolean> condition, Supplier<T> supplier, T defaultValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(supplier, "supplier cannot be null");
        
        if (condition.get()) {
            return supplier.get();
        }
        return defaultValue;
    }
    
    /**
     * 批量执行 Supplier
     * 
     * @param suppliers 供应者列表
     * @param <T> 返回类型
     * @return 结果列表
     */
    @SafeVarargs
    public static <T> List<T> forEach(Supplier<T>... suppliers) {
        Objects.requireNonNull(suppliers, "suppliers cannot be null");
        if (suppliers.length == 0) {
            throw new IllegalArgumentException("suppliers array cannot be empty");
        }
        
        List<T> results = List.empty();
        for (Supplier<T> supplier : suppliers) {
            Objects.requireNonNull(supplier, "supplier in array cannot be null");
            results = results.append(supplier.get());
        }
        return results;
    }
    
    /**
     * 批量执行 Supplier，收集所有结果（包括失败）
     * 
     * @param suppliers 供应者列表
     * @param <T> 返回类型
     * @return Try 结果列表
     */
    @SafeVarargs
    public static <T> List<Try<T>> forEachCollect(Supplier<T>... suppliers) {
        Objects.requireNonNull(suppliers, "suppliers cannot be null");
        
        return List.of(suppliers).map(VavrCheckedSupplier::execute);
    }
    
    /**
     * 链式执行多个 Supplier
     * 
     * @param suppliers 供应者列表
     * @param <T> 返回类型
     * @return 最终结果
     */
    @SafeVarargs
    public static <T> T chain(Supplier<T>... suppliers) {
        Objects.requireNonNull(suppliers, "suppliers cannot be null");
        if (suppliers.length == 0) {
            throw new IllegalArgumentException("suppliers array cannot be empty");
        }
        
        T result = null;
        for (Supplier<T> supplier : suppliers) {
            Objects.requireNonNull(supplier, "supplier in array cannot be null");
            result = supplier.get();
        }
        return result;
    }
    
    /**
     * 创建带异步的 Supplier
     * 
     * @param supplier 原始供应者
     * @param <T> 返回类型
     * @return 异步 Supplier
     */
    public static <T> Supplier<CompletableFuture<T>> withAsync(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        
        return () -> CompletableFuture.supplyAsync(supplier);
    }
    
    /**
     * 创建带并行处理的 Supplier
     * 
     * @param suppliers 供应者列表
     * @param <T> 返回类型
     * @return 并行处理 Supplier
     */
    @SafeVarargs
    public static <T> Supplier<java.util.List<T>> withParallel(Supplier<T>... suppliers) {
        Objects.requireNonNull(suppliers, "suppliers cannot be null");
        if (suppliers.length == 0) {
            throw new IllegalArgumentException("suppliers array cannot be empty");
        }
        
        return () -> {
            try {
                return java.util.Arrays.stream(suppliers)
                        .parallel()
                        .map(Supplier::get)
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Parallel execution failed", e);
            }
        };
    }
    
    /**
     * 创建带延迟的 Supplier
     * 
     * @param supplier 原始供应者
     * @param delayMs 延迟时间（毫秒）
     * @param <T> 返回类型
     * @return 带延迟的 Supplier
     */
    public static <T> Supplier<T> withDelay(Supplier<T> supplier, long delayMs) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must be non-negative");
        }
        
        return () -> {
            try {
                Thread.sleep(delayMs);
                return supplier.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Operation was interrupted", e);
            }
        };
    }
    
    /**
     * 创建带重复执行的 Supplier
     * 
     * @param supplier 原始供应者
     * @param times 重复次数
     * @param <T> 返回类型
     * @return 带重复执行的 Supplier
     */
    public static <T> Supplier<java.util.List<T>> withRepeat(Supplier<T> supplier, int times) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (times <= 0) {
            throw new IllegalArgumentException("times must be positive");
        }
        
        return () -> {
            java.util.List<T> results = new java.util.ArrayList<>();
            for (int i = 0; i < times; i++) {
                results.add(supplier.get());
            }
            return results;
        };
    }
} 