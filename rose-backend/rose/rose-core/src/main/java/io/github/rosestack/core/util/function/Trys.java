package io.github.rosestack.core.util.function;

import io.github.rosestack.core.util.function.checked.CheckedConsumer;
import io.github.rosestack.core.util.function.checked.CheckedFunction;
import io.github.rosestack.core.util.function.checked.CheckedSupplier;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Try 相关的工具方法集合
 * 提供异常安全的函数式编程工具
 *
 * @author rose
 */
@Slf4j
public final class Trys {

    private Trys() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Function 相关方法 ====================

    /**
     * 创建异常安全的 Function
     *
     * @param function 可能抛出异常的函数
     * @param <T>      输入类型
     * @param <R>      返回类型
     * @return 异常安全的 Function
     */
    public static <T, R> Function<T, R> apply(CheckedFunction<T, R> function) {
        return apply(function, null, null);
    }

    /**
     * 创建异常安全的 Function，带错误处理
     *
     * @param function     可能抛出异常的函数
     * @param errorHandler 错误处理函数
     * @param <T>          输入类型
     * @param <R>          返回类型
     * @return 异常安全的 Function
     */
    public static <T, R> Function<T, R> apply(
            CheckedFunction<T, R> function, CheckedFunction<Throwable, R> errorHandler) {
        return apply(function, errorHandler, null);
    }

    /**
     * 创建异常安全的 Function，带错误处理和最终处理
     *
     * @param function        可能抛出异常的函数
     * @param errorHandler    错误处理函数
     * @param finallyConsumer 最终处理消费者
     * @param <T>             输入类型
     * @param <R>             返回类型
     * @return 异常安全的 Function
     */
    public static <T, R> Function<T, R> apply(
            CheckedFunction<T, R> function,
            CheckedFunction<Throwable, R> errorHandler,
            CheckedConsumer<T> finallyConsumer) {
        Objects.requireNonNull(function, "function cannot be null");
        return input -> {
            try {
                return function.apply(input);
            } catch (Exception e) {
                log.warn("Function execution failed", e);
                if (errorHandler != null) {
                    try {
                        return errorHandler.apply(e);
                    } catch (Exception handlerException) {
                        log.error("Error handler failed", handlerException);
						Sneaky.sneakyThrow(handlerException);
                    }
                }
				Sneaky.sneakyThrow(e);
                return null; // 永远不会执行到这里
            } finally {
                if (finallyConsumer != null) {
                    try {
                        finallyConsumer.accept(input);
                    } catch (Exception finallyException) {
                        log.warn("Finally block failed", finallyException);
                    }
                }
            }
        };
    }

    /**
     * 创建条件性的异常安全 Function
     *
     * @param condition    执行条件
     * @param function     可能抛出异常的函数
     * @param defaultValue 条件不满足时的默认值
     * @param <T>          输入类型
     * @param <R>          返回类型
     * @return 异常安全的 Function
     */
    public static <T, R> Function<T, R> applyIf(
            Predicate<T> condition, CheckedFunction<T, R> function, R defaultValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        return input -> {
            if (condition.test(input)) {
                return apply(function).apply(input);
            }
            return defaultValue;
        };
    }

    // ==================== Consumer 相关方法 ====================

    /**
     * 创建异常安全的 Consumer
     *
     * @param consumer 可能抛出异常的消费者
     * @param <T>      输入类型
     * @return 异常安全的 Consumer
     */
    public static <T> Consumer<T> accept(CheckedConsumer<T> consumer) {
        return accept(consumer, null, null);
    }

    /**
     * 创建异常安全的 Consumer，带错误处理
     *
     * @param consumer     可能抛出异常的消费者
     * @param errorHandler 错误处理函数
     * @param <T>          输入类型
     * @return 异常安全的 Consumer
     */
    public static <T> Consumer<T> accept(CheckedConsumer<T> consumer, CheckedConsumer<Throwable> errorHandler) {
        return accept(consumer, errorHandler, null);
    }

    /**
     * 创建异常安全的 Consumer，带错误处理和最终处理
     *
     * @param consumer        可能抛出异常的消费者
     * @param errorHandler    错误处理消费者
     * @param finallyConsumer 最终处理消费者
     * @param <T>             输入类型
     * @return 异常安全的 Consumer
     */
    public static <T> Consumer<T> accept(
            CheckedConsumer<T> consumer, CheckedConsumer<Throwable> errorHandler, CheckedConsumer<T> finallyConsumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return input -> {
            try {
                consumer.accept(input);
            } catch (Exception e) {
                log.warn("Consumer execution failed", e);
                if (errorHandler != null) {
                    try {
                        errorHandler.accept(e);
                    } catch (Exception handlerException) {
                        log.error("Error handler failed", handlerException);
                    }
                }
				Sneaky.sneakyThrow(e);
            } finally {
                if (finallyConsumer != null) {
                    try {
                        finallyConsumer.accept(input);
                    } catch (Exception finallyException) {
                        log.warn("Finally block failed", finallyException);
                    }
                }
            }
        };
    }

    /**
     * 创建条件性的异常安全 Consumer
     *
     * @param condition 执行条件
     * @param consumer  可能抛出异常的消费者
     * @param <T>       输入类型
     * @return 异常安全的 Consumer
     */
    public static <T> Consumer<T> acceptIf(Predicate<T> condition, CheckedConsumer<T> consumer) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return input -> {
            if (condition.test(input)) {
                accept(consumer).accept(input);
            }
        };
    }

    // ==================== Supplier 相关方法 ====================

    /**
     * 创建异常安全的 Supplier
     *
     * @param supplier 可能抛出异常的供应者
     * @param <T>      返回类型
     * @return 异常安全的 Supplier
     */
    public static <T> Supplier<T> get(CheckedSupplier<T> supplier) {
        return get(supplier, null);
    }

    /**
     * 创建异常安全的 Supplier，带错误处理
     *
     * @param supplier     可能抛出异常的供应者
     * @param errorHandler 错误处理函数
     * @param <T>          返回类型
     * @return 异常安全的 Supplier
     */
    public static <T> Supplier<T> get(CheckedSupplier<T> supplier, CheckedFunction<Throwable, T> errorHandler) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.warn("Supplier execution failed", e);
                if (errorHandler != null) {
                    try {
                        return errorHandler.apply(e);
                    } catch (Exception handlerException) {
                        log.error("Error handler failed", handlerException);
						Sneaky.sneakyThrow(handlerException);
                    }
                }
				Sneaky.sneakyThrow(e);
                return null; // 永远不会执行到这里
            }
        };
    }

    /**
     * 创建条件性的异常安全 Supplier
     *
     * @param condition    执行条件
     * @param supplier     可能抛出异常的供应者
     * @param defaultValue 条件不满足时的默认值
     * @param <T>          返回类型
     * @return 异常安全的 Supplier
     */
    public static <T> Supplier<T> getIf(boolean condition, CheckedSupplier<T> supplier, T defaultValue) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return () -> {
            if (condition) {
                return get(supplier).get();
            }
            return defaultValue;
        };
    }

    // ==================== 便利方法 ====================

    /**
     * 安全执行 Runnable
     *
     * @param runnable 可能抛出异常的 Runnable
     */
    public static void run(CheckedConsumer<Void> runnable) {
        accept(runnable).accept(null);
    }

    /**
     * 安全执行 Runnable，带错误处理
     *
     * @param runnable     可能抛出异常的 Runnable
     * @param errorHandler 错误处理消费者
     */
    public static void run(CheckedConsumer<Void> runnable, CheckedConsumer<Throwable> errorHandler) {
        accept(runnable, errorHandler).accept(null);
    }
}
