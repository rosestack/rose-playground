package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedConsumer;
import io.github.rose.core.lang.function.core.Try;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * CheckedConsumer 工具类
 * 提供基于 CheckedConsumer 的便捷方法
 *
 * @author rose
 */
public final class CheckedConsumerUtils {

    private CheckedConsumerUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将 CheckedConsumer 转换为标准 Consumer
     *
     * @param checkedConsumer 受检异常的消费者
     * @param <T>             输入类型
     * @return 标准 Consumer
     */
    public static <T> Consumer<T> unchecked(CheckedConsumer<T> checkedConsumer) {
        Objects.requireNonNull(checkedConsumer, "checkedConsumer cannot be null");
        return checkedConsumer.unchecked();
    }

    /**
     * 将标准 Consumer 转换为 CheckedConsumer
     *
     * @param consumer 标准消费者
     * @param <T>      输入类型
     * @return 受检异常的消费者
     */
    public static <T> CheckedConsumer<T> checked(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return CheckedConsumer.from(consumer);
    }

    // ==================== 批量处理方法 ====================

    /**
     * 批量处理集合中的每个元素
     *
     * @param consumer   消费者
     * @param collection 集合
     * @param <T>        元素类型
     */
    public static <T> void forEach(CheckedConsumer<T> consumer, java.util.Collection<T> collection) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        for (T item : collection) {
            try {
                consumer.accept(item);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 批量处理集合中的每个元素，收集所有结果（包括失败）
     *
     * @param consumer   消费者
     * @param collection 集合
     * @param <T>        元素类型
     * @return Try 结果列表
     */
    public static <T> java.util.List<Try<Void>> forEachCollect(CheckedConsumer<T> consumer,
                                                               java.util.Collection<T> collection) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        return collection.stream()
                .map(item -> Try.ofConsumer(item, consumer))
                .collect(java.util.stream.Collectors.toList());
    }

    // ==================== 链式处理方法 ====================

    /**
     * 链式处理值
     *
     * @param value     要处理的值
     * @param consumers 消费者列表
     * @param <T>       值类型
     */
    @SafeVarargs
    public static <T> void chain(T value, CheckedConsumer<T>... consumers) {
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(consumers, "consumers cannot be null");

        for (CheckedConsumer<T> consumer : consumers) {
            Objects.requireNonNull(consumer, "consumer in array cannot be null");
            try {
                consumer.accept(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ==================== 条件处理方法 ====================

    /**
     * 条件处理值
     *
     * @param value     要处理的值
     * @param condition 条件函数
     * @param consumer  消费者
     * @param <T>       值类型
     */
    public static <T> void conditional(T value, java.util.function.Predicate<T> condition,
                                       CheckedConsumer<T> consumer) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");

        if (condition.test(value)) {
            try {
                consumer.accept(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 创建空操作消费者
     *
     * @param <T> 输入类型
     * @return 空操作消费者
     */
    public static <T> CheckedConsumer<T> noop() {
        return input -> {
            // 什么都不做
        };
    }

    /**
     * 创建异常消费者
     *
     * @param exception 要抛出的异常
     * @param <T>       输入类型
     * @return 异常消费者
     */
    public static <T> CheckedConsumer<T> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return input -> {
            throw exception;
        };
    }

    /**
     * 创建日志消费者
     *
     * @param message 日志消息
     * @param <T>     输入类型
     * @return 日志消费者
     */
    public static <T> CheckedConsumer<T> log(String message) {
        Objects.requireNonNull(message, "message cannot be null");
        return input -> {
            System.out.println(message + ": " + input);
        };
    }

    /**
     * 创建打印消费者
     *
     * @param <T> 输入类型
     * @return 打印消费者
     */
    public static <T> CheckedConsumer<T> print() {
        return input -> {
            System.out.println(input);
        };
    }


}