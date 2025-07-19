package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function1;
import io.vavr.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Vavr CheckedBiConsumer 工具类
 * 提供 CheckedBiConsumer 相关的功能方法
 * 
 * @author rose
 */
public class VavrCheckedBiConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedBiConsumer.class);
    
    /**
     * 执行 CheckedBiConsumer
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param consumer 可能抛出异常的双参数消费者
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return Try 结果
     */
    public static <T, U> Try<Void> checkedBiConsumer(T t, U u, Function2<T, U, Try<Void>> consumer) {
        return consumer.apply(t, u);
    }
    
    /**
     * 执行 CheckedBiConsumer，带异常恢复
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param consumer 可能抛出异常的双参数消费者
     * @param fallback 异常恢复函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return Try 结果
     */
    public static <T, U> Try<Void> checkedBiConsumer(T t, U u, Function2<T, U, Try<Void>> consumer,
                                                     Function1<Throwable, Try<Void>> fallback) {
        return consumer.apply(t, u).recoverWith(fallback);
    }
    
    /**
     * 批量执行 CheckedBiConsumer
     * 
     * @param items 输入项列表
     * @param param 固定参数
     * @param consumer 可能抛出异常的双参数消费者
     * @param <T> 输入类型
     * @param <U> 固定参数类型
     * @return Try 结果，任一失败则整体失败
     */
    public static <T, U> Try<Void> forEachWithParam(Iterable<T> items, U param, 
                                                   Function2<T, U, Try<Void>> consumer) {
        return Try.of(() -> {
            for (T item : items) {
                consumer.apply(item, param).get(); // 如果失败会抛出异常
            }
            return null;
        });
    }
    
    /**
     * 带日志的 CheckedBiConsumer
     * 
     * @param t 第一个参数
     * @param u 第二个参数
     * @param consumer 可能抛出异常的双参数消费者
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return Try 结果
     */
    public static <T, U> Try<Void> loggedBiConsumer(T t, U u, Function2<T, U, Try<Void>> consumer, String operationName) {
        log.debug("Starting operation: {} with params: ({}, {})", operationName, t, u);
        
        Try<Void> result = consumer.apply(t, u);
        
        if (result.isSuccess()) {
            log.debug("Operation {} completed successfully", operationName);
        } else {
            log.error("Operation {} failed: {}", operationName, result.getCause().getMessage());
        }
        
        return result;
    }
    
    /**
     * 将标准 BiConsumer 转换为 CheckedBiConsumer
     * 
     * @param consumer 标准双参数消费者
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return CheckedBiConsumer
     */
    public static <T, U> Function2<T, U, Try<Void>> toCheckedBiConsumer(BiConsumer<T, U> consumer) {
        return (t, u) -> Try.of(() -> {
            consumer.accept(t, u);
            return null;
        });
    }
} 