package io.github.rosestack.core.lang.function.checked;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 受检异常的消费者接口 对应 JDK 的 Consumer<T>，但可以抛出受检异常
 *
 * @param <T> 输入参数类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * 从 JDK Consumer 创建 CheckedConsumer
     */
    static <T> CheckedConsumer<T> from(Consumer<T> consumer) {
        Objects.requireNonNull(consumer);
        return consumer::accept;
    }

    /**
     * 消费值
     *
     * @param t 输入参数
     * @throws Exception 可能抛出的异常
     */
    void accept(T t) throws Exception;

    /**
     * 组合消费者
     */
    default CheckedConsumer<T> andThen(CheckedConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * 组合消费者
     */
    default CheckedConsumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * 转换为 JDK Consumer（异常会被包装为 RuntimeException）
     */
    default Consumer<T> unchecked() {
        return (T t) -> {
            try {
                accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 转换为 JDK Consumer，使用自定义异常处理器
     *
     * @param handler 异常处理器，接收捕获的异常
     * @return 标准 Consumer
     */
    default Consumer<T> unchecked(java.util.function.Consumer<Throwable> handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        return (T t) -> {
            try {
                accept(t);
            } catch (Exception e) {
                handler.accept(e);
            }
        };
    }
}
