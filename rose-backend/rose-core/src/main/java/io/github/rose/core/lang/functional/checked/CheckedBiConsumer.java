package io.github.rose.core.lang.functional.checked;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 受检异常的双参数消费者接口
 * 对应 JDK 的 BiConsumer<T, U>，但可以抛出受检异常
 * 
 * @param <T> 第一个输入参数类型
 * @param <U> 第二个输入参数类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedBiConsumer<T, U> {
    
    /**
     * 消费两个值
     * 
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @throws Exception 可能抛出的异常
     */
    void accept(T t, U u) throws Exception;
    
    /**
     * 组合消费者
     */
    default CheckedBiConsumer<T, U> andThen(CheckedBiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> {
            accept(t, u);
            after.accept(t, u);
        };
    }
    
    /**
     * 组合消费者
     */
    default CheckedBiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> {
            accept(t, u);
            after.accept(t, u);
        };
    }
    
    /**
     * 部分应用：固定第一个参数
     */
    default CheckedConsumer<U> applyFirst(T t) {
        return (U u) -> accept(t, u);
    }
    
    /**
     * 部分应用：固定第二个参数
     */
    default CheckedConsumer<T> applySecond(U u) {
        return (T t) -> accept(t, u);
    }
    
    /**
     * 转换为 JDK BiConsumer（异常会被包装为 RuntimeException）
     */
    default BiConsumer<T, U> unchecked() {
        return (T t, U u) -> {
            try {
                accept(t, u);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 从 JDK BiConsumer 创建 CheckedBiConsumer
     */
    static <T, U> CheckedBiConsumer<T, U> from(BiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer);
        return consumer::accept;
    }
    
    /**
     * 创建空消费者
     */
    static <T, U> CheckedBiConsumer<T, U> noop() {
        return (t, u) -> {};
    }
}