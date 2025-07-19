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
import java.util.function.Consumer;

/**
 * Vavr Consumer 工具类
 * 提供对 Vavr Function1 和标准 Consumer 的工具方法和转换功能
 * 
 * @author rose
 */
public class VavrCheckedConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedConsumer.class);
    
    // ==================== 转换方法 ====================
    
    /**
     * 将标准 Consumer 转换为 Vavr Function1
     * 
     * @param consumer 标准消费者
     * @param <T> 输入类型
     * @return Vavr Function1
     */
    public static <T> Function1<T, Void> toFunction1(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return value -> {
            consumer.accept(value);
            return null;
        };
    }
    
    /**
     * 将标准 Consumer 转换为 Vavr Function1<Try<Void>>
     * 
     * @param consumer 标准消费者
     * @param <T> 输入类型
     * @return Vavr Function1<Try<Void>>
     */
    public static <T> Function1<T, Try<Void>> toFunction1Try(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return value -> Try.of(() -> {
            consumer.accept(value);
            return null;
        });
    }
    
    /**
     * 将 Vavr Function1 转换为标准 Consumer
     * 
     * @param function Vavr 函数
     * @param <T> 输入类型
     * @return 标准 Consumer
     */
    public static <T> Consumer<T> toConsumer(Function1<T, Void> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return value -> function.apply(value);
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 执行 Consumer 并返回 Try
     * 
     * @param consumer 消费者
     * @param value 输入值
     * @param <T> 输入类型
     * @return Try 结果
     */
    public static <T> Try<Void> execute(Consumer<T> consumer, T value) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return Try.of(() -> {
            consumer.accept(value);
            return null;
        });
    }
    
    /**
     * 执行 Vavr Function1 并返回 Try
     * 
     * @param function Vavr 函数
     * @param value 输入值
     * @param <T> 输入类型
     * @return Try 结果
     */
    public static <T> Try<Void> execute(Function1<T, Void> function, T value) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> {
            function.apply(value);
            return null;
        });
    }
    
    // ==================== 装饰器方法 ====================
    
    /**
     * 创建带重试的 Consumer
     * 
     * @param consumer 原始消费者
     * @param maxRetries 最大重试次数
     * @param <T> 输入类型
     * @return 带重试的 Consumer
     */
    public static <T> Consumer<T> withRetry(Consumer<T> consumer, int maxRetries) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        
        return value -> {
            Try<Void> result = Try.of(() -> {
                consumer.accept(value);
                return null;
            });
            int retries = 0;
            
            while (result.isFailure() && retries < maxRetries) {
                log.warn("Consumer failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
                result = Try.of(() -> {
                    consumer.accept(value);
                    return null;
                });
                retries++;
            }
            
            if (result.isFailure()) {
                throw new RuntimeException(result.getCause());
            }
        };
    }
    
    /**
     * 创建带超时的 Consumer
     * 
     * @param consumer 原始消费者
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 输入类型
     * @return 带超时的 Consumer
     */
    public static <T> Consumer<T> withTimeout(Consumer<T> consumer, long timeoutMs) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        
        return value -> {
            try {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> consumer.accept(value));
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 创建带日志的 Consumer
     * 
     * @param consumer 原始消费者
     * @param operationName 操作名称
     * @param <T> 输入类型
     * @return 带日志的 Consumer
     */
    public static <T> Consumer<T> withLogging(Consumer<T> consumer, String operationName) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return value -> {
            log.debug("Starting consumer operation: {} with value: {}", operationName, value);
            
            try {
                consumer.accept(value);
                log.debug("Consumer operation {} completed successfully", operationName);
            } catch (Exception e) {
                log.error("Consumer operation {} failed: {}", operationName, e.getMessage());
                throw e;
            }
        };
    }
    
    /**
     * 创建带降级的 Consumer
     * 
     * @param primary 主要消费者
     * @param fallback 降级消费者
     * @param <T> 输入类型
     * @return 带降级的 Consumer
     */
    public static <T> Consumer<T> withFallback(Consumer<T> primary, Consumer<T> fallback) {
        Objects.requireNonNull(primary, "primary consumer cannot be null");
        Objects.requireNonNull(fallback, "fallback consumer cannot be null");
        
        return value -> {
            try {
                primary.accept(value);
            } catch (Exception e) {
                log.warn("Primary consumer failed, using fallback: {}", e.getMessage());
                fallback.accept(value);
            }
        };
    }
    
    /**
     * 创建带验证的 Consumer
     * 
     * @param consumer 原始消费者
     * @param validator 验证器
     * @param <T> 输入类型
     * @return 带验证的 Consumer
     */
    public static <T> Consumer<T> withValidation(Consumer<T> consumer, Function1<T, Boolean> validator) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");
        
        return value -> {
            if (!validator.apply(value)) {
                throw new IllegalArgumentException("Validation failed for value: " + value);
            }
            consumer.accept(value);
        };
    }
    
    /**
     * 创建带异常处理的 Consumer
     * 
     * @param consumer 原始消费者
     * @param exceptionHandler 异常处理器
     * @param <T> 输入类型
     * @return 带异常处理的 Consumer
     */
    public static <T> Consumer<T> withExceptionHandling(Consumer<T> consumer, 
                                                       Function1<Throwable, Void> exceptionHandler) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        
        return value -> {
            try {
                consumer.accept(value);
            } catch (Exception e) {
                exceptionHandler.apply(e);
            }
        };
    }
    
    /**
     * 创建带指标的 Consumer
     * 
     * @param consumer 原始消费者
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 输入类型
     * @return 带指标的 Consumer
     */
    public static <T> Consumer<T> withMetrics(Consumer<T> consumer,
                                             Function1<Long, Void> metricsCollector,
                                             String operationName) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(metricsCollector, "metricsCollector cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return value -> {
            long startTime = System.currentTimeMillis();
            try {
                consumer.accept(value);
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply(duration);
                throw e;
            }
        };
    }
    
    // ==================== 组合方法 ====================
    
    /**
     * 条件执行 Consumer
     * 
     * @param value 输入值
     * @param condition 条件
     * @param consumer 消费者
     * @param <T> 输入类型
     */
    public static <T> void conditional(T value, Function1<T, Boolean> condition, Consumer<T> consumer) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");
        
        if (condition.apply(value)) {
            consumer.accept(value);
        }
    }
    
    /**
     * 批量执行 Consumer
     * 
     * @param items 输入项列表
     * @param consumer 消费者
     * @param <T> 输入类型
     */
    public static <T> void forEach(Iterable<T> items, Consumer<T> consumer) {
        Objects.requireNonNull(items, "items cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");
        
        for (T item : items) {
            consumer.accept(item);
        }
    }
    
    /**
     * 批量执行 Consumer，收集所有结果（包括失败）
     * 
     * @param items 输入项列表
     * @param consumer 消费者
     * @param <T> 输入类型
     * @return Try 结果列表
     */
    public static <T> List<Try<Void>> forEachCollect(Iterable<T> items, Consumer<T> consumer) {
        Objects.requireNonNull(items, "items cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");
        
        return List.ofAll(items).map(item -> Try.of(() -> {
            consumer.accept(item);
            return null;
        }));
    }
    
    /**
     * 链式执行多个 Consumer
     * 
     * @param value 输入值
     * @param consumers 消费者列表
     * @param <T> 输入类型
     */
    @SafeVarargs
    public static <T> void chain(T value, Consumer<T>... consumers) {
        Objects.requireNonNull(consumers, "consumers cannot be null");
        if (consumers.length == 0) {
            throw new IllegalArgumentException("consumers array cannot be empty");
        }
        
        for (Consumer<T> consumer : consumers) {
            Objects.requireNonNull(consumer, "consumer in array cannot be null");
            consumer.accept(value);
        }
    }
    
    /**
     * 创建带异步的 Consumer
     * 
     * @param consumer 原始消费者
     * @param <T> 输入类型
     * @return 异步 Consumer
     */
    public static <T> Consumer<T> withAsync(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        
        return value -> CompletableFuture.runAsync(() -> consumer.accept(value));
    }
    
    /**
     * 创建带并行处理的 Consumer
     * 
     * @param consumers 消费者列表
     * @param <T> 输入类型
     * @return 并行处理 Consumer
     */
    @SafeVarargs
    public static <T> Consumer<T> withParallel(Consumer<T>... consumers) {
        Objects.requireNonNull(consumers, "consumers cannot be null");
        if (consumers.length == 0) {
            throw new IllegalArgumentException("consumers array cannot be empty");
        }
        
        return value -> {
            try {
                java.util.List<CompletableFuture<Void>> futures = java.util.Arrays.stream(consumers)
                        .parallel()
                        .map(consumer -> CompletableFuture.runAsync(() -> consumer.accept(value)))
                        .collect(java.util.stream.Collectors.toList());
                
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (Exception e) {
                throw new RuntimeException("Parallel execution failed", e);
            }
        };
    }
    
    /**
     * 创建带批处理的 Consumer
     * 
     * @param consumer 原始消费者
     * @param batchSize 批处理大小
     * @param <T> 输入类型
     * @return 批处理 Consumer
     */
    public static <T> Consumer<java.util.List<T>> withBatchProcessing(Consumer<T> consumer, int batchSize) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        
        return values -> {
            Objects.requireNonNull(values, "values cannot be null");
            try {
                values.parallelStream().forEach(consumer);
            } catch (Exception e) {
                throw new RuntimeException("Batch processing failed", e);
            }
        };
    }
} 