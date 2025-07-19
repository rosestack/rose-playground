package io.github.rose.core.lang.function;

import io.vavr.control.Option;
import io.vavr.Function1;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Vavr Option 工具类
 * 提供 Option 相关的功能方法
 * 
 * @author rose
 */
public class VavrOption {
    
    private static final Logger log = LoggerFactory.getLogger(VavrOption.class);
    
    /**
     * 安全地从可能为空的值创建 Option
     * 
     * @param value 可能为空的值
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> of(T value) {
        return Option.of(value);
    }
    
    /**
     * 从可能抛出异常的 Supplier 创建 Option
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> ofSupplier(Supplier<T> supplier) {
        try {
            return Option.of(supplier.get());
        } catch (Exception e) {
            log.debug("Supplier threw exception: {}", e.getMessage());
            return Option.none();
        }
    }
    
    /**
     * 从可能抛出异常的函数创建 Option
     * 
     * @param value 输入值
     * @param function 可能抛出异常的函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Option
     */
    public static <T, R> Option<R> ofFunction(T value, Function<T, R> function) {
        try {
            return Option.of(function.apply(value));
        } catch (Exception e) {
            log.debug("Function threw exception: {}", e.getMessage());
            return Option.none();
        }
    }
    
    /**
     * 带默认值的 Option 转换
     * 
     * @param option 原始 Option
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 转换后的值
     */
    public static <T> T getOrElse(Option<T> option, T defaultValue) {
        return option.getOrElse(defaultValue);
    }
    
    /**
     * 带默认值 Supplier 的 Option 转换
     * 
     * @param option 原始 Option
     * @param defaultSupplier 默认值 Supplier
     * @param <T> 值类型
     * @return 转换后的值
     */
    public static <T> T getOrElse(Option<T> option, Supplier<T> defaultSupplier) {
        return option.getOrElse(defaultSupplier);
    }
    
    /**
     * 带异常处理的 Option 转换
     * 
     * @param option 原始 Option
     * @param exceptionHandler 异常处理函数
     * @param <T> 值类型
     * @return 转换后的值
     */
    public static <T> T getOrElseThrow(Option<T> option, Supplier<? extends RuntimeException> exceptionHandler) {
        return option.getOrElseThrow(exceptionHandler);
    }
    
    /**
     * 条件转换 Option
     * 
     * @param option 原始 Option
     * @param predicate 条件
     * @param mapper 转换函数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Option
     */
    public static <T, R> Option<R> conditionalMap(Option<T> option, Predicate<T> predicate, Function<T, R> mapper) {
        return option.filter(predicate).map(mapper);
    }
    
    /**
     * 带日志的 Option 转换
     * 
     * @param option 原始 Option
     * @param mapper 转换函数
     * @param operationName 操作名称
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Option
     */
    public static <T, R> Option<R> loggedMap(Option<T> option, Function<T, R> mapper, String operationName) {
        if (option.isDefined()) {
            log.debug("Starting option operation: {} with value: {}", operationName, option.get());
            try {
                R result = mapper.apply(option.get());
                log.debug("Option operation {} completed successfully with result: {}", operationName, result);
                return Option.of(result);
            } catch (Exception e) {
                log.error("Option operation {} failed: {}", operationName, e.getMessage());
                return Option.none();
            }
        } else {
            log.debug("Option operation {} skipped - no value present", operationName);
            return Option.none();
        }
    }
    
    /**
     * 带重试的 Option 转换
     * 
     * @param option 原始 Option
     * @param mapper 转换函数
     * @param maxRetries 最大重试次数
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Option
     */
    public static <T, R> Option<R> retryMap(Option<T> option, Function<T, R> mapper, int maxRetries) {
        if (!option.isDefined()) {
            return Option.none();
        }
        
        int retries = 0;
        while (retries <= maxRetries) {
            try {
                R result = mapper.apply(option.get());
                return Option.of(result);
            } catch (Exception e) {
                retries++;
                if (retries > maxRetries) {
                    log.warn("Option map failed after {} retries: {}", maxRetries, e.getMessage());
                    return Option.none();
                }
                log.debug("Option map failed, retrying... (attempt {}/{})", retries, maxRetries);
            }
        }
        return Option.none();
    }
    
    /**
     * 批量处理 Option 列表
     * 
     * @param options Option 列表
     * @param <T> 值类型
     * @return 所有有值的列表
     */
    public static <T> List<T> collectDefined(Iterable<Option<T>> options) {
        return List.ofAll(options).flatMap(Option::toList);
    }
    
    /**
     * 批量处理 Option 列表，保留所有结果
     * 
     * @param options Option 列表
     * @param <T> 值类型
     * @return 原始 Option 列表
     */
    public static <T> List<Option<T>> preserveAll(Iterable<Option<T>> options) {
        return List.ofAll(options);
    }
    
    /**
     * 链式处理多个 Option
     * 
     * @param option 初始 Option
     * @param mappers 转换函数列表
     * @param <T> 初始类型
     * @param <R> 最终类型
     * @return 最终 Option
     */
    @SafeVarargs
    public static <T, R> Option<R> chainMap(Option<T> option, Function<T, Option<R>>... mappers) {
        Option<R> result = Option.none();
        for (Function<T, Option<R>> mapper : mappers) {
            result = option.flatMap(mapper);
            if (result.isDefined()) {
                break;
            }
        }
        return result;
    }
    
    /**
     * 创建带缓存的 Option
     * 
     * @param supplier 值提供者
     * @param cache 缓存对象
     * @param key 缓存键
     * @param <T> 值类型
     * @return 带缓存的 Option
     */
    public static <T> Option<T> withCache(Supplier<T> supplier, java.util.Map<String, T> cache, String key) {
        if (cache.containsKey(key)) {
            return Option.of(cache.get(key));
        }
        
        Option<T> result = ofSupplier(supplier);
        if (result.isDefined()) {
            cache.put(key, result.get());
        }
        
        return result;
    }
    
    /**
     * 创建带验证的 Option
     * 
     * @param value 原始值
     * @param validator 验证器
     * @param <T> 值类型
     * @return 验证后的 Option
     */
    public static <T> Option<T> withValidation(T value, Predicate<T> validator) {
        return Option.of(value).filter(validator);
    }
    
    /**
     * 创建带转换的 Option
     * 
     * @param value 原始值
     * @param transformer 转换器
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 转换后的 Option
     */
    public static <T, R> Option<R> withTransform(T value, Function<T, R> transformer) {
        return ofFunction(value, transformer);
    }
    
    /**
     * 创建带条件的 Option
     * 
     * @param value 原始值
     * @param condition 条件
     * @param <T> 值类型
     * @return 条件满足时的 Option
     */
    public static <T> Option<T> withCondition(T value, Predicate<T> condition) {
        return Option.of(value).filter(condition);
    }
    
    /**
     * 创建带异常处理的 Option
     * 
     * @param supplier 值提供者
     * @param exceptionHandler 异常处理函数
     * @param <T> 值类型
     * @return 异常处理后的 Option
     */
    public static <T> Option<T> withExceptionHandling(Supplier<T> supplier, 
                                                     Function1<Throwable, T> exceptionHandler) {
        try {
            return Option.of(supplier.get());
        } catch (Exception e) {
            return Option.of(exceptionHandler.apply(e));
        }
    }
    
    /**
     * 创建带超时的 Option
     * 
     * @param supplier 值提供者
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 值类型
     * @return 超时处理后的 Option
     */
    public static <T> Option<T> withTimeout(Supplier<T> supplier, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        try {
            T result = supplier.get();
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                log.warn("Operation timed out after {}ms", timeoutMs);
                return Option.none();
            }
            return Option.of(result);
        } catch (Exception e) {
            log.error("Operation failed: {}", e.getMessage());
            return Option.none();
        }
    }
    
    /**
     * 创建带重试的 Option
     * 
     * @param supplier 值提供者
     * @param maxRetries 最大重试次数
     * @param <T> 值类型
     * @return 重试处理后的 Option
     */
    public static <T> Option<T> withRetry(Supplier<T> supplier, int maxRetries) {
        int retries = 0;
        while (retries <= maxRetries) {
            try {
                return Option.of(supplier.get());
            } catch (Exception e) {
                retries++;
                if (retries > maxRetries) {
                    log.warn("Operation failed after {} retries: {}", maxRetries, e.getMessage());
                    return Option.none();
                }
                log.debug("Operation failed, retrying... (attempt {}/{})", retries, maxRetries);
            }
        }
        return Option.none();
    }
} 