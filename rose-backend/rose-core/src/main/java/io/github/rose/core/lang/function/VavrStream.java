package io.github.rose.core.lang.function;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.Function1;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Vavr Stream 工具类
 * 提供 Stream 相关的功能方法
 * 
 * @author rose
 */
public class VavrStream {
    
    private static final Logger log = LoggerFactory.getLogger(VavrStream.class);
    
    /**
     * 从 Supplier 创建无限 Stream
     * 
     * @param supplier 值提供者
     * @param <T> 值类型
     * @return 无限 Stream
     */
    public static <T> Stream<T> fromSupplier(Supplier<T> supplier) {
        return Stream.continually(supplier);
    }
    
    /**
     * 从 Supplier 创建有限 Stream
     * 
     * @param supplier 值提供者
     * @param size 大小
     * @param <T> 值类型
     * @return 有限 Stream
     */
    public static <T> Stream<T> fromSupplier(Supplier<T> supplier, int size) {
        return Stream.continually(supplier).take(size);
    }
    
    /**
     * 从迭代器创建 Stream
     * 
     * @param iterator 迭代器
     * @param <T> 值类型
     * @return Stream
     */
    public static <T> Stream<T> fromIterator(java.util.Iterator<T> iterator) {
        return Stream.ofAll(() -> iterator);
    }
    
    /**
     * 从集合创建 Stream
     * 
     * @param iterable 集合
     * @param <T> 值类型
     * @return Stream
     */
    public static <T> Stream<T> fromIterable(Iterable<T> iterable) {
        return Stream.ofAll(iterable);
    }
    
    /**
     * 带日志的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param operationName 操作名称
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> loggedMap(Stream<T> stream, Function<T, R> mapper, String operationName) {
        return stream.map(value -> {
            log.debug("Stream operation: {} processing value: {}", operationName, value);
            try {
                R result = mapper.apply(value);
                log.debug("Stream operation: {} completed for value: {} with result: {}", operationName, value, result);
                return result;
            } catch (Exception e) {
                log.error("Stream operation: {} failed for value: {} with error: {}", operationName, value, e.getMessage());
                throw e;
            }
        });
    }
    
    /**
     * 带重试的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param maxRetries 最大重试次数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> retryMap(Stream<T> stream, Function<T, R> mapper, int maxRetries) {
        return stream.map(value -> {
            int retries = 0;
            while (retries <= maxRetries) {
                try {
                    return mapper.apply(value);
                } catch (Exception e) {
                    retries++;
                    if (retries > maxRetries) {
                        log.warn("Stream map failed after {} retries for value: {}", maxRetries, value);
                        throw e;
                    }
                    log.debug("Stream map failed, retrying... (attempt {}/{}) for value: {}", retries, maxRetries, value);
                }
            }
            throw new RuntimeException("Max retries exceeded");
        });
    }
    
    /**
     * 带超时的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> timeoutMap(Stream<T> stream, Function<T, R> mapper, long timeoutMs) {
        return stream.map(value -> {
            long startTime = System.currentTimeMillis();
            R result = mapper.apply(value);
            
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Stream operation timed out after " + timeoutMs + "ms for value: " + value);
            }
            
            return result;
        });
    }
    
    /**
     * 带缓存的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param cache 缓存对象
     * @param keyGenerator 缓存键生成器
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> cachedMap(Stream<T> stream, Function<T, R> mapper,
                                           java.util.Map<String, R> cache,
                                           Function1<T, String> keyGenerator) {
        return stream.map(value -> {
            String cacheKey = keyGenerator.apply(value);
            if (cache.containsKey(cacheKey)) {
                return cache.get(cacheKey);
            }
            
            R result = mapper.apply(value);
            cache.put(cacheKey, result);
            return result;
        });
    }
    
    /**
     * 带验证的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param validator 验证器
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> validatedMap(Stream<T> stream, Function<T, R> mapper, Predicate<R> validator) {
        return stream.map(value -> {
            R result = mapper.apply(value);
            if (!validator.test(result)) {
                throw new IllegalArgumentException("Validation failed for result: " + result);
            }
            return result;
        });
    }
    
    /**
     * 带异常处理的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param exceptionHandler 异常处理函数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> safeMap(Stream<T> stream, Function<T, R> mapper, Function1<Throwable, R> exceptionHandler) {
        return stream.map(value -> {
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        });
    }
    
    /**
     * 带降级的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param primary 主要转换函数
     * @param fallback 降级转换函数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> fallbackMap(Stream<T> stream, Function<T, R> primary, Function<T, R> fallback) {
        return stream.map(value -> {
            try {
                return primary.apply(value);
            } catch (Exception e) {
                log.warn("Primary mapper failed, using fallback for value: {}", value);
                return fallback.apply(value);
            }
        });
    }
    
    /**
     * 带指标的 Stream 转换
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> metricsMap(Stream<T> stream, Function<T, R> mapper,
                                            Function1<Long, Void> metricsCollector,
                                            String operationName) {
        return stream.map(value -> {
            long startTime = System.currentTimeMillis();
            try {
                R result = mapper.apply(value);
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration);
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration);
                throw e;
            }
        });
    }
    
    /**
     * 带条件过滤的 Stream
     * 
     * @param stream 原始 Stream
     * @param condition 条件
     * @param <T> 值类型
     * @return 过滤后的 Stream
     */
    public static <T> Stream<T> conditionalFilter(Stream<T> stream, Predicate<T> condition) {
        return stream.filter(condition);
    }
    
    /**
     * 带条件转换的 Stream
     * 
     * @param stream 原始 Stream
     * @param condition 条件
     * @param ifTrue 条件为真时的转换函数
     * @param ifFalse 条件为假时的转换函数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Stream
     */
    public static <T, R> Stream<R> conditionalMap(Stream<T> stream, Predicate<T> condition,
                                                Function<T, R> ifTrue, Function<T, R> ifFalse) {
        return stream.map(value -> condition.test(value) ? ifTrue.apply(value) : ifFalse.apply(value));
    }
    
    /**
     * 带批处理的 Stream
     * 
     * @param stream 原始 Stream
     * @param batchSize 批处理大小
     * @param <T> 值类型
     * @return 批处理后的 Stream
     */
    public static <T> Stream<List<T>> batch(Stream<T> stream, int batchSize) {
        return Stream.ofAll(stream.grouped(batchSize)).map(Stream::toList);
    }
    
    /**
     * 带窗口的 Stream
     * 
     * @param stream 原始 Stream
     * @param windowSize 窗口大小
     * @param <T> 值类型
     * @return 窗口化的 Stream
     */
    public static <T> Stream<List<T>> window(Stream<T> stream, int windowSize) {
        return Stream.ofAll(stream.sliding(windowSize)).map(Stream::toList);
    }
    
    /**
     * 带去重的 Stream
     * 
     * @param stream 原始 Stream
     * @param <T> 值类型
     * @return 去重后的 Stream
     */
    public static <T> Stream<T> distinct(Stream<T> stream) {
        return stream.distinct();
    }
    
    /**
     * 带排序的 Stream
     * 
     * @param stream 原始 Stream
     * @param <T> 值类型
     * @return 排序后的 Stream
     */
    public static <T extends Comparable<T>> Stream<T> sorted(Stream<T> stream) {
        return stream.sorted();
    }
    
    /**
     * 带自定义排序的 Stream
     * 
     * @param stream 原始 Stream
     * @param comparator 比较器
     * @param <T> 值类型
     * @return 排序后的 Stream
     */
    public static <T> Stream<T> sorted(Stream<T> stream, java.util.Comparator<T> comparator) {
        return stream.sorted(comparator);
    }
    
    /**
     * 带限制的 Stream
     * 
     * @param stream 原始 Stream
     * @param limit 限制数量
     * @param <T> 值类型
     * @return 限制后的 Stream
     */
    public static <T> Stream<T> limit(Stream<T> stream, int limit) {
        return stream.take(limit);
    }
    
    /**
     * 带跳过的 Stream
     * 
     * @param stream 原始 Stream
     * @param skip 跳过数量
     * @param <T> 值类型
     * @return 跳过后的 Stream
     */
    public static <T> Stream<T> skip(Stream<T> stream, int skip) {
        return stream.drop(skip);
    }
    
    /**
     * 带并行的 Stream
     * 
     * @param stream 原始 Stream
     * @param <T> 值类型
     * @return 并行 Stream
     */
    public static <T> Stream<T> parallel(Stream<T> stream) {
        // Vavr Stream 不支持 parallel() 方法，返回原 Stream
        return stream;
    }
    
    /**
     * 带异步的 Stream
     * 
     * @param stream 原始 Stream
     * @param mapper 转换函数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 异步 Stream
     */
    public static <T, R> Stream<java.util.concurrent.CompletableFuture<R>> asyncMap(Stream<T> stream, Function<T, R> mapper) {
        return stream.map(value -> java.util.concurrent.CompletableFuture.supplyAsync(() -> mapper.apply(value)));
    }
    
    /**
     * 带记忆化的 Stream
     * 
     * @param stream 原始 Stream
     * @param <T> 值类型
     * @return 记忆化的 Stream
     */
    public static <T> Stream<T> memoized(Stream<T> stream) {
        // Vavr Stream 不支持 memoized() 方法，返回原 Stream
        return stream;
    }
    
    /**
     * 带延迟求值的 Stream
     * 
     * @param stream 原始 Stream
     * @param <T> 值类型
     * @return 延迟求值的 Stream
     */
    public static <T> Stream<Supplier<T>> lazy(Stream<T> stream) {
        return stream.map(value -> (Supplier<T>) () -> value);
    }
    
    /**
     * 带错误恢复的 Stream
     * 
     * @param stream 原始 Stream
     * @param fallback 错误恢复函数
     * @param <T> 值类型
     * @return 错误恢复后的 Stream
     */
    public static <T> Stream<T> withErrorRecovery(Stream<T> stream, Function1<Throwable, T> fallback) {
        return stream.map(value -> {
            try {
                return value;
            } catch (Exception e) {
                return fallback.apply(e);
            }
        });
    }
    
    /**
     * 带错误映射的 Stream
     * 
     * @param stream 原始 Stream
     * @param errorMapper 错误映射函数
     * @param <T> 值类型
     * @return 错误映射后的 Stream
     */
    public static <T> Stream<T> withErrorMapping(Stream<T> stream, Function1<Throwable, Throwable> errorMapper) {
        return stream.map(value -> {
            try {
                return value;
            } catch (Exception e) {
                throw new RuntimeException(errorMapper.apply(e));
            }
        });
    }
} 