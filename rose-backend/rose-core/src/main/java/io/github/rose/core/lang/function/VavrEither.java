package io.github.rose.core.lang.function;

import io.vavr.control.Either;
import io.vavr.Function1;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Vavr Either 工具类
 * 提供 Either 相关的功能方法
 * 
 * @author rose
 */
public class VavrEither {
    
    private static final Logger log = LoggerFactory.getLogger(VavrEither.class);
    
    /**
     * 从可能抛出异常的 Supplier 创建 Either
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 成功类型
     * @return Either
     */
    public static <T> Either<Throwable, T> ofSupplier(Supplier<T> supplier) {
        try {
            return Either.right(supplier.get());
        } catch (Exception e) {
            return Either.left(e);
        }
    }
    
    /**
     * 从可能抛出异常的函数创建 Either
     * 
     * @param value 输入值
     * @param function 可能抛出异常的函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Either
     */
    public static <T, R> Either<Throwable, R> ofFunction(T value, Function<T, R> function) {
        try {
            return Either.right(function.apply(value));
        } catch (Exception e) {
            return Either.left(e);
        }
    }
    
    /**
     * 带错误转换的 Either 处理
     * 
     * @param either 原始 Either
     * @param errorMapper 错误转换函数
     * @param <L> 原始错误类型
     * @param <R> 成功类型
     * @param <L2> 新错误类型
     * @return 转换后的 Either
     */
    public static <L, R, L2> Either<L2, R> mapLeft(Either<L, R> either, Function<L, L2> errorMapper) {
        return either.mapLeft(errorMapper);
    }
    
    /**
     * 带成功转换的 Either 处理
     * 
     * @param either 原始 Either
     * @param successMapper 成功转换函数
     * @param <L> 错误类型
     * @param <R> 原始成功类型
     * @param <R2> 新成功类型
     * @return 转换后的 Either
     */
    public static <L, R, R2> Either<L, R2> mapRight(Either<L, R> either, Function<R, R2> successMapper) {
        return either.map(successMapper);
    }
    
    /**
     * 带日志的 Either 处理
     * 
     * @param either 原始 Either
     * @param operationName 操作名称
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 原始 Either
     */
    public static <L, R> Either<L, R> logged(Either<L, R> either, String operationName) {
        if (either.isRight()) {
            log.debug("Either operation {} completed successfully with result: {}", operationName, either.get());
        } else {
            log.error("Either operation {} failed: {}", operationName, either.getLeft());
        }
        return either;
    }
    
    /**
     * 带重试的 Either 处理
     * 
     * @param supplier 值提供者
     * @param maxRetries 最大重试次数
     * @param <T> 值类型
     * @return Either
     */
    public static <T> Either<Throwable, T> withRetry(Supplier<T> supplier, int maxRetries) {
        int retries = 0;
        while (retries <= maxRetries) {
            Either<Throwable, T> result = ofSupplier(supplier);
            if (result.isRight()) {
                return result;
            }
            retries++;
            if (retries > maxRetries) {
                log.warn("Operation failed after {} retries: {}", maxRetries, result.getLeft().getMessage());
                return result;
            }
            log.debug("Operation failed, retrying... (attempt {}/{})", retries, maxRetries);
        }
        return Either.left(new RuntimeException("Max retries exceeded"));
    }
    
    /**
     * 带超时的 Either 处理
     * 
     * @param supplier 值提供者
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 值类型
     * @return Either
     */
    public static <T> Either<Throwable, T> withTimeout(Supplier<T> supplier, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        Either<Throwable, T> result = ofSupplier(supplier);
        
        if (System.currentTimeMillis() - startTime > timeoutMs) {
            return Either.left(new RuntimeException("Operation timed out after " + timeoutMs + "ms"));
        }
        
        return result;
    }
    
    /**
     * 带默认值的 Either 处理
     * 
     * @param either 原始 Either
     * @param defaultValue 默认值
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 成功值或默认值
     */
    public static <L, R> R getOrElse(Either<L, R> either, R defaultValue) {
        return either.getOrElse(defaultValue);
    }
    
    /**
     * 带默认值 Supplier 的 Either 处理
     * 
     * @param either 原始 Either
     * @param defaultSupplier 默认值 Supplier
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 成功值或默认值
     */
    public static <L, R> R getOrElse(Either<L, R> either, Supplier<R> defaultSupplier) {
        return either.getOrElse(defaultSupplier);
    }
    
    /**
     * 带异常抛出的 Either 处理
     * 
     * @param either 原始 Either
     * @param exceptionMapper 异常映射函数
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 成功值或抛出异常
     */
    public static <L, R> R getOrElseThrow(Either<L, R> either, Function<L, ? extends RuntimeException> exceptionMapper) {
        return either.getOrElseThrow(exceptionMapper);
    }
    
    /**
     * 批量处理 Either 列表
     * 
     * @param eithers Either 列表
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 所有成功值的列表，任一失败则整体失败
     */
    public static <L, R> Either<L, List<R>> forEach(Iterable<Either<L, R>> eithers) {
        List<R> results = List.empty();
        for (Either<L, R> either : eithers) {
            if (either.isLeft()) {
                return Either.left(either.getLeft());
            }
            results = results.append(either.get());
        }
        return Either.right(results);
    }
    
    /**
     * 批量处理 Either 列表，收集所有结果
     * 
     * @param eithers Either 列表
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 原始 Either 列表
     */
    public static <L, R> List<Either<L, R>> forEachCollect(Iterable<Either<L, R>> eithers) {
        return List.ofAll(eithers);
    }
    
    /**
     * 链式处理多个 Either
     * 
     * @param either 初始 Either
     * @param mapper 转换函数
     * @param <L> 错误类型
     * @param <R> 初始成功类型
     * @param <R2> 最终成功类型
     * @return 最终 Either
     */
    public static <L, R, R2> Either<L, R2> chainMap(Either<L, R> either, Function<R, Either<L, R2>> mapper) {
        return either.flatMap(mapper);
    }
    
    /**
     * 创建带缓存的 Either
     * 
     * @param supplier 值提供者
     * @param cache 缓存对象
     * @param key 缓存键
     * @param <T> 值类型
     * @return 带缓存的 Either
     */
    public static <T> Either<Throwable, T> withCache(Supplier<T> supplier, java.util.Map<String, T> cache, String key) {
        if (cache.containsKey(key)) {
            return Either.right(cache.get(key));
        }
        
        Either<Throwable, T> result = ofSupplier(supplier);
        if (result.isRight()) {
            cache.put(key, result.get());
        }
        
        return result;
    }
    
    /**
     * 创建带验证的 Either
     * 
     * @param value 原始值
     * @param validator 验证器
     * @param errorMessage 错误消息
     * @param <T> 值类型
     * @return 验证后的 Either
     */
    public static <T> Either<String, T> withValidation(T value, Function1<T, Boolean> validator, String errorMessage) {
        if (validator.apply(value)) {
            return Either.right(value);
        } else {
            return Either.left(errorMessage);
        }
    }
    
    /**
     * 创建带转换的 Either
     * 
     * @param value 原始值
     * @param transformer 转换器
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Either
     */
    public static <T, R> Either<Throwable, R> withTransform(T value, Function<T, R> transformer) {
        return ofFunction(value, transformer);
    }
    
    /**
     * 创建带条件的 Either
     * 
     * @param value 原始值
     * @param condition 条件
     * @param errorMessage 错误消息
     * @param <T> 值类型
     * @return 条件满足时的 Either
     */
    public static <T> Either<String, T> withCondition(T value, Function1<T, Boolean> condition, String errorMessage) {
        if (condition.apply(value)) {
            return Either.right(value);
        } else {
            return Either.left(errorMessage);
        }
    }
    
    /**
     * 创建带异常处理的 Either
     * 
     * @param supplier 值提供者
     * @param exceptionHandler 异常处理函数
     * @param <T> 值类型
     * @return 异常处理后的 Either
     */
    public static <T> Either<Throwable, T> withExceptionHandling(Supplier<T> supplier, 
                                                               Function1<Throwable, T> exceptionHandler) {
        try {
            return Either.right(supplier.get());
        } catch (Exception e) {
            return Either.right(exceptionHandler.apply(e));
        }
    }
    
    /**
     * 创建带错误恢复的 Either
     * 
     * @param either 原始 Either
     * @param fallback 错误恢复函数
     * @param <L> 错误类型
     * @param <R> 成功类型
     * @return 错误恢复后的 Either
     */
    public static <L, R> Either<L, R> withFallback(Either<L, R> either, Function1<L, R> fallback) {
        if (either.isRight()) {
            return either;
        } else {
            return Either.right(fallback.apply(either.getLeft()));
        }
    }
    
    /**
     * 创建带错误映射的 Either
     * 
     * @param either 原始 Either
     * @param errorMapper 错误映射函数
     * @param <L> 原始错误类型
     * @param <R> 成功类型
     * @param <L2> 新错误类型
     * @return 错误映射后的 Either
     */
    public static <L, R, L2> Either<L2, R> withErrorMapping(Either<L, R> either, Function1<L, L2> errorMapper) {
        return either.mapLeft(errorMapper);
    }
    
    /**
     * 创建带成功映射的 Either
     * 
     * @param either 原始 Either
     * @param successMapper 成功映射函数
     * @param <L> 错误类型
     * @param <R> 原始成功类型
     * @param <R2> 新成功类型
     * @return 成功映射后的 Either
     */
    public static <L, R, R2> Either<L, R2> withSuccessMapping(Either<L, R> either, Function1<R, R2> successMapper) {
        return either.map(successMapper);
    }
} 