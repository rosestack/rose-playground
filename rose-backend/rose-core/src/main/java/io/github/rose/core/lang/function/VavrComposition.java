package io.github.rose.core.lang.function;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function4;
import io.vavr.Function5;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Vavr 函数组合工具类
 * 提供函数组合相关的功能方法
 * 
 * @author rose
 */
public class VavrComposition {
    
    private static final Logger log = LoggerFactory.getLogger(VavrComposition.class);
    
    /**
     * 函数组合：f ∘ g = f(g(x))
     * 
     * @param f 外层函数
     * @param g 内层函数
     * @param <T> 输入类型
     * @param <U> 中间类型
     * @param <R> 输出类型
     * @return 组合后的函数
     */
    public static <T, U, R> Function1<T, R> compose(Function1<U, R> f, Function1<T, U> g) {
        return f.compose(g);
    }
    
    /**
     * 函数组合：g ∘ f = g(f(x))
     * 
     * @param f 内层函数
     * @param g 外层函数
     * @param <T> 输入类型
     * @param <U> 中间类型
     * @param <R> 输出类型
     * @return 组合后的函数
     */
    public static <T, U, R> Function1<T, R> andThen(Function1<T, U> f, Function1<U, R> g) {
        return f.andThen(g);
    }
    
    /**
     * 管道操作：从左到右依次应用函数
     * 
     * @param value 初始值
     * @param functions 函数列表
     * @param <T> 值类型
     * @return 最终结果
     */
    @SafeVarargs
    public static <T> T pipe(T value, Function1<T, T>... functions) {
        T result = value;
        for (Function1<T, T> function : functions) {
            result = function.apply(result);
        }
        return result;
    }
    
    /**
     * 管道操作：从左到右依次应用函数，支持类型转换
     * 
     * @param value 初始值
     * @param functions 函数列表
     * @param <T> 初始类型
     * @param <R> 最终类型
     * @return 最终结果
     */
    @SafeVarargs
    public static <T, R> R pipe(T value, Function<Object, Object>... functions) {
        Object result = value;
        for (Function<Object, Object> function : functions) {
            result = function.apply(result);
        }
        @SuppressWarnings("unchecked")
        R finalResult = (R) result;
        return finalResult;
    }
    
    /**
     * 部分应用：固定第一个参数
     * 
     * @param function 双参数函数
     * @param first 第一个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 单参数函数
     */
    public static <T, U, R> Function1<U, R> partial(T function, Function2<T, U, R> first) {
        return first.apply(function);
    }
    
    /**
     * 部分应用：固定第二个参数
     * 
     * @param function 双参数函数
     * @param second 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 单参数函数
     */
    public static <T, U, R> Function1<T, R> partialSecond(Function2<T, U, R> function, U second) {
        return t -> function.apply(t, second);
    }
    
    /**
     * 柯里化：将双参数函数转换为单参数函数链
     * 
     * @param function 双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 柯里化后的函数
     */
    public static <T, U, R> Function1<T, Function1<U, R>> curry(Function2<T, U, R> function) {
        return t -> u -> function.apply(t, u);
    }
    
    /**
     * 反柯里化：将柯里化函数转换为双参数函数
     * 
     * @param curried 柯里化函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 双参数函数
     */
    public static <T, U, R> Function2<T, U, R> uncurry(Function1<T, Function1<U, R>> curried) {
        return (t, u) -> curried.apply(t).apply(u);
    }
    
    /**
     * 条件组合：根据条件选择不同的函数
     * 
     * @param condition 条件
     * @param ifTrue 条件为真时的函数
     * @param ifFalse 条件为假时的函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 条件函数
     */
    public static <T, R> Function1<T, R> conditional(Predicate<T> condition, 
                                                    Function1<T, R> ifTrue, 
                                                    Function1<T, R> ifFalse) {
        return value -> condition.test(value) ? ifTrue.apply(value) : ifFalse.apply(value);
    }
    
    /**
     * 带日志的函数组合
     * 
     * @param function 原始函数
     * @param operationName 操作名称
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带日志的函数
     */
    public static <T, R> Function1<T, R> withLogging(Function1<T, R> function, String operationName) {
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
     * 带重试的函数组合
     * 
     * @param function 原始函数
     * @param maxRetries 最大重试次数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带重试的函数
     */
    public static <T, R> Function1<T, R> withRetry(Function1<T, R> function, int maxRetries) {
        return value -> {
            int retries = 0;
            while (retries <= maxRetries) {
                try {
                    return function.apply(value);
                } catch (Exception e) {
                    retries++;
                    if (retries > maxRetries) {
                        log.warn("Function failed after {} retries: {}", maxRetries, e.getMessage());
                        throw e;
                    }
                    log.debug("Function failed, retrying... (attempt {}/{})", retries, maxRetries);
                }
            }
            throw new RuntimeException("Max retries exceeded");
        };
    }
    
    /**
     * 带超时的函数组合
     * 
     * @param function 原始函数
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带超时的函数
     */
    public static <T, R> Function1<T, R> withTimeout(Function1<T, R> function, long timeoutMs) {
        return value -> {
            long startTime = System.currentTimeMillis();
            R result = function.apply(value);
            
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            }
            
            return result;
        };
    }
    
    /**
     * 带缓存的函数组合
     * 
     * @param function 原始函数
     * @param cache 缓存对象
     * @param keyGenerator 缓存键生成器
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带缓存的函数
     */
    public static <T, R> Function1<T, R> withCache(Function1<T, R> function,
                                                  java.util.Map<String, R> cache,
                                                  Function1<T, String> keyGenerator) {
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
    
    /**
     * 带验证的函数组合
     * 
     * @param function 原始函数
     * @param validator 验证器
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带验证的函数
     */
    public static <T, R> Function1<T, R> withValidation(Function1<T, R> function, Predicate<R> validator) {
        return value -> {
            R result = function.apply(value);
            if (!validator.test(result)) {
                throw new IllegalArgumentException("Validation failed for result: " + result);
            }
            return result;
        };
    }
    
    /**
     * 带异常处理的函数组合
     * 
     * @param function 原始函数
     * @param exceptionHandler 异常处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带异常处理的函数
     */
    public static <T, R> Function1<T, R> withExceptionHandling(Function1<T, R> function,
                                                              Function1<Throwable, R> exceptionHandler) {
        return value -> {
            try {
                return function.apply(value);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }
    
    /**
     * 带降级的函数组合
     * 
     * @param primary 主要函数
     * @param fallback 降级函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带降级的函数
     */
    public static <T, R> Function1<T, R> withFallback(Function1<T, R> primary, Function1<T, R> fallback) {
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
     * 带指标的函数组合
     * 
     * @param function 原始函数
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带指标的函数
     */
    public static <T, R> Function1<T, R> withMetrics(Function1<T, R> function,
                                                    Function1<Long, Void> metricsCollector,
                                                    String operationName) {
        return value -> {
            long startTime = System.currentTimeMillis();
            try {
                R result = function.apply(value);
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
    
    /**
     * 带断路器的函数组合
     * 
     * @param function 原始函数
     * @param circuitBreaker 断路器
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带断路器的函数
     */
    public static <T, R> Function1<T, R> withCircuitBreaker(Function1<T, R> function,
                                                           Function1<Function1<T, R>, Function1<T, R>> circuitBreaker) {
        return circuitBreaker.apply(function);
    }
    
    /**
     * 带限流的函数组合
     * 
     * @param function 原始函数
     * @param rateLimiter 限流器
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带限流的函数
     */
    public static <T, R> Function1<T, R> withRateLimiter(Function1<T, R> function,
                                                        Function1<Function1<T, R>, Function1<T, R>> rateLimiter) {
        return rateLimiter.apply(function);
    }
    
    /**
     * 带异步的函数组合
     * 
     * @param function 原始函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 异步函数
     */
    public static <T, R> Function1<T, java.util.concurrent.CompletableFuture<R>> withAsync(Function1<T, R> function) {
        return value -> java.util.concurrent.CompletableFuture.supplyAsync(() -> function.apply(value));
    }
    
    /**
     * 带并行处理的函数组合
     * 
     * @param functions 函数列表
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 并行处理函数
     */
    @SafeVarargs
    public static <T, R> Function1<T, java.util.List<R>> withParallel(Function1<T, R>... functions) {
        return value -> {
            return java.util.Arrays.stream(functions)
                    .parallel()
                    .map(f -> f.apply(value))
                    .collect(java.util.stream.Collectors.toList());
        };
    }
    
    /**
     * 带顺序处理的函数组合
     * 
     * @param functions 函数列表
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 顺序处理函数
     */
    @SafeVarargs
    public static <T, R> Function1<T, java.util.List<R>> withSequential(Function1<T, R>... functions) {
        return value -> {
            return java.util.Arrays.stream(functions)
                    .map(f -> f.apply(value))
                    .collect(java.util.stream.Collectors.toList());
        };
    }
} 