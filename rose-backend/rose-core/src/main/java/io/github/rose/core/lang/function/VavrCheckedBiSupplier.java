package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Vavr CheckedBiSupplier 工具类
 * 提供 CheckedBiSupplier 相关的功能方法
 * 
 * @author rose
 */
public class VavrCheckedBiSupplier {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedBiSupplier.class);
    
    /**
     * 执行 CheckedBiSupplier
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> checkedBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier) {
        return supplier.apply(t, u);
    }
    
    /**
     * 执行 CheckedBiSupplier，带异常恢复
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param fallback 异常恢复函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> checkedBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier,
                                                     Function1<Throwable, Try<R>> fallback) {
        return supplier.apply(t, u).recoverWith(fallback);
    }
    
    /**
     * 执行 CheckedBiSupplier，带默认值
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param defaultValue 默认值
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> checkedBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier, R defaultValue) {
        return supplier.apply(t, u).recover(throwable -> defaultValue);
    }
    
    /**
     * 条件执行 CheckedBiSupplier
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param condition 条件
     * @param supplier 可能抛出异常的双参数供应者
     * @param defaultValue 默认值
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> conditionalBiSupplier(T t, U u, Function2<T, U, Boolean> condition,
                                                        Function2<T, U, Try<R>> supplier, R defaultValue) {
        if (condition.apply(t, u)) {
            return supplier.apply(t, u);
        }
        return Try.success(defaultValue);
    }
    
    /**
     * 带重试的 CheckedBiSupplier
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param maxRetries 最大重试次数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> retryBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier, int maxRetries) {
        return Try.of(() -> {
            Try<R> result = supplier.apply(t, u);
            int retries = 0;
            
            while (result.isFailure() && retries < maxRetries) {
                log.warn("BiSupplier failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
                result = supplier.apply(t, u);
                retries++;
            }
            
            if (result.isFailure()) {
                throw result.getCause();
            }
            
            return result.get();
        });
    }
    
    /**
     * 带超时的 CheckedBiSupplier
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> timeoutBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier, long timeoutMs) {
        return Try.of(() -> {
            long startTime = System.currentTimeMillis();
            Try<R> result = supplier.apply(t, u);
            
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
     * 缓存 CheckedBiSupplier 结果
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param cacheKey 缓存键
     * @param cache 缓存对象
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> cachedBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier,
                                                   String cacheKey, java.util.Map<String, R> cache) {
        if (cache.containsKey(cacheKey)) {
            return Try.success(cache.get(cacheKey));
        }
        
        Try<R> result = supplier.apply(t, u);
        if (result.isSuccess()) {
            cache.put(cacheKey, result.get());
        }
        
        return result;
    }
    
    /**
     * 批量执行 CheckedBiSupplier
     * 
     * @param items 输入项列表
     * @param param 固定参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param <T> 输入类型
     * @param <U> 固定参数类型
     * @param <R> 返回类型
     * @return Try 结果，任一失败则整体失败
     */
    public static <T, U, R> Try<List<R>> forEachWithParam(Iterable<T> items, U param, 
                                                         Function2<T, U, Try<R>> supplier) {
        return Try.of(() -> {
            List<R> results = List.empty();
            for (T item : items) {
                results = results.append(supplier.apply(item, param).get());
            }
            return results;
        });
    }
    
    /**
     * 批量执行 CheckedBiSupplier，收集所有结果
     * 
     * @param items 输入项列表
     * @param param 固定参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param <T> 输入类型
     * @param <U> 固定参数类型
     * @param <R> 返回类型
     * @return 所有结果的列表
     */
    public static <T, U, R> List<Try<R>> forEachWithParamCollect(Iterable<T> items, U param, 
                                                                Function2<T, U, Try<R>> supplier) {
        return List.ofAll(items).map(item -> supplier.apply(item, param));
    }
    
    /**
     * 链式执行 CheckedBiSupplier
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param suppliers 供应者列表
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    @SafeVarargs
    public static <T, U, R> Try<R> chainBiSuppliers(T t, U u, Function2<T, U, Try<R>>... suppliers) {
        return Try.of(() -> {
            R result = null;
            for (Function2<T, U, Try<R>> supplier : suppliers) {
                result = supplier.apply(t, u).get();
            }
            return result;
        });
    }
    
    /**
     * 带日志的 CheckedBiSupplier
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param supplier 可能抛出异常的双参数供应者
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> loggedBiSupplier(T t, U u, Function2<T, U, Try<R>> supplier, String operationName) {
        log.debug("Starting biSupplier operation: {} with params: ({}, {})", operationName, t, u);
        
        Try<R> result = supplier.apply(t, u);
        
        if (result.isSuccess()) {
            log.debug("BiSupplier operation {} completed successfully with result: {}", operationName, result.get());
        } else {
            log.error("BiSupplier operation {} failed: {}", operationName, result.getCause().getMessage());
        }
        
        return result;
    }
    
    /**
     * 将标准 BiFunction 转换为 CheckedBiSupplier
     * 
     * @param function 标准双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return CheckedBiSupplier
     */
    public static <T, U, R> Function2<T, U, Try<R>> toCheckedBiSupplier(BiFunction<T, U, R> function) {
        return (t, u) -> Try.of(() -> function.apply(t, u));
    }
    
    /**
     * 创建带缓存的 CheckedBiSupplier
     * 
     * @param supplier 原始供应者
     * @param cache 缓存对象
     * @param keyGenerator 缓存键生成器
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带缓存的 CheckedBiSupplier
     */
    public static <T, U, R> Function2<T, U, Try<R>> withCache(Function2<T, U, Try<R>> supplier,
                                                             java.util.Map<String, R> cache,
                                                             Function2<T, U, String> keyGenerator) {
        return (t, u) -> {
            String cacheKey = keyGenerator.apply(t, u);
            if (cache.containsKey(cacheKey)) {
                return Try.success(cache.get(cacheKey));
            }
            
            Try<R> result = supplier.apply(t, u);
            if (result.isSuccess()) {
                cache.put(cacheKey, result.get());
            }
            
            return result;
        };
    }
    
    /**
     * 创建带重试的 CheckedBiSupplier
     * 
     * @param supplier 原始供应者
     * @param maxRetries 最大重试次数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带重试的 CheckedBiSupplier
     */
    public static <T, U, R> Function2<T, U, Try<R>> withRetry(Function2<T, U, Try<R>> supplier, int maxRetries) {
        return (t, u) -> retryBiSupplier(t, u, supplier, maxRetries);
    }
    
    /**
     * 创建带超时的 CheckedBiSupplier
     * 
     * @param supplier 原始供应者
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带超时的 CheckedBiSupplier
     */
    public static <T, U, R> Function2<T, U, Try<R>> withTimeout(Function2<T, U, Try<R>> supplier, long timeoutMs) {
        return (t, u) -> timeoutBiSupplier(t, u, supplier, timeoutMs);
    }
    
    /**
     * 创建带日志的 CheckedBiSupplier
     * 
     * @param supplier 原始供应者
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带日志的 CheckedBiSupplier
     */
    public static <T, U, R> Function2<T, U, Try<R>> withLogging(Function2<T, U, Try<R>> supplier, String operationName) {
        return (t, u) -> loggedBiSupplier(t, u, supplier, operationName);
    }
} 