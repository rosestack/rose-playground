package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedBiConsumer;
import io.github.rose.core.lang.function.core.Try;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * CheckedBiConsumer 工具类
 * 提供基于 CheckedBiConsumer 的便捷方法
 *
 * @author rose
 */
public final class CheckedBiConsumerUtils {

    private CheckedBiConsumerUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将 CheckedBiConsumer 转换为标准 BiConsumer
     *
     * @param checkedConsumer 受检异常的双参数消费者
     * @param <T>             第一个参数类型
     * @param <U>             第二个参数类型
     * @return 标准 BiConsumer
     */
    public static <T, U> BiConsumer<T, U> unchecked(CheckedBiConsumer<T, U> checkedConsumer) {
        Objects.requireNonNull(checkedConsumer, "checkedConsumer cannot be null");
        return checkedConsumer.unchecked();
    }

    /**
     * 将标准 BiConsumer 转换为 CheckedBiConsumer
     *
     * @param consumer 标准双参数消费者
     * @param <T>      第一个参数类型
     * @param <U>      第二个参数类型
     * @return 受检异常的双参数消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> checked(BiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return CheckedBiConsumer.from(consumer);
    }

    // ==================== 执行方法 ====================

    /**
     * 执行受检异常的双参数消费者
     *
     * @param consumer 受检异常的双参数消费者
     * @param t        第一个参数
     * @param u        第二个参数
     * @param <T>      第一个参数类型
     * @param <U>      第二个参数类型
     */
    public static <T, U> void execute(CheckedBiConsumer<T, U> consumer, T t, U u) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(t, u);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 异步执行方法 ====================

    /**
     * 异步执行受检异常的双参数消费者
     *
     * @param consumer 受检异常的双参数消费者
     * @param t        第一个参数
     * @param u        第二个参数
     * @param <T>      第一个参数类型
     * @param <U>      第二个参数类型
     * @return CompletableFuture
     */
    public static <T, U> CompletableFuture<Void> async(CheckedBiConsumer<T, U> consumer, T t, U u) {
        return async(consumer, t, u, null);
    }

    /**
     * 异步执行受检异常的双参数消费者
     *
     * @param consumer 受检异常的双参数消费者
     * @param t        第一个参数
     * @param u        第二个参数
     * @param executor 执行器
     * @param <T>      第一个参数类型
     * @param <U>      第二个参数类型
     * @return CompletableFuture
     */
    public static <T, U> CompletableFuture<Void> async(CheckedBiConsumer<T, U> consumer, T t, U u, Executor executor) {
        Objects.requireNonNull(consumer, "consumer cannot be null");

        if (executor != null) {
            return CompletableFuture.runAsync(() -> execute(consumer, t, u), executor);
        } else {
            return CompletableFuture.runAsync(() -> execute(consumer, t, u));
        }
    }

    // ==================== 批量处理方法 ====================

    /**
     * 固定第一个参数，遍历第二个参数集合
     *
     * @param consumer   受检异常的双参数消费者
     * @param t          固定的第一个参数
     * @param collection 第二个参数的集合
     * @param <T>        第一个参数类型
     * @param <U>        第二个参数类型
     */
    public static <T, U> void forEachWithFirst(CheckedBiConsumer<T, U> consumer, T t, java.util.Collection<U> collection) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        for (U u : collection) {
            execute(consumer, t, u);
        }
    }

    /**
     * 固定第二个参数，遍历第一个参数集合
     *
     * @param consumer   受检异常的双参数消费者
     * @param collection 第一个参数的集合
     * @param u          固定的第二个参数
     * @param <T>        第一个参数类型
     * @param <U>        第二个参数类型
     */
    public static <T, U> void forEachWithSecond(CheckedBiConsumer<T, U> consumer, java.util.Collection<T> collection, U u) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        for (T t : collection) {
            execute(consumer, t, u);
        }
    }

    /**
     * 固定第一个参数，遍历第二个参数集合，收集所有结果（包括失败）
     *
     * @param consumer   受检异常的双参数消费者
     * @param t          固定的第一个参数
     * @param collection 第二个参数的集合
     * @param <T>        第一个参数类型
     * @param <U>        第二个参数类型
     * @return Try 结果列表
     */
    public static <T, U> java.util.List<Try<Void>> forEachWithFirstCollect(CheckedBiConsumer<T, U> consumer, T t, java.util.Collection<U> collection) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        return collection.stream()
                .map(u -> Try.ofConsumer(t, u, consumer))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 固定第二个参数，遍历第一个参数集合，收集所有结果（包括失败）
     *
     * @param consumer   受检异常的双参数消费者
     * @param collection 第一个参数的集合
     * @param u          固定的第二个参数
     * @param <T>        第一个参数类型
     * @param <U>        第二个参数类型
     * @return Try 结果列表
     */
    public static <T, U> java.util.List<Try<Void>> forEachWithSecondCollect(CheckedBiConsumer<T, U> consumer, java.util.Collection<T> collection, U u) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        return collection.stream()
                .map(t -> Try.ofConsumer(t, u, consumer))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 应用到 Map 的每个键值对
     *
     * @param consumer 受检异常的双参数消费者
     * @param map      Map 集合
     * @param <T>      键类型
     * @param <U>      值类型
     */
    public static <T, U> void forEachMap(CheckedBiConsumer<T, U> consumer, java.util.Map<T, U> map) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(map, "map cannot be null");

        for (java.util.Map.Entry<T, U> entry : map.entrySet()) {
            execute(consumer, entry.getKey(), entry.getValue());
        }
    }

    // ==================== 重试方法 ====================

    /**
     * 带重试的 CheckedBiConsumer
     *
     * @param consumer    原始双参数消费者
     * @param t           第一个参数
     * @param u           第二个参数
     * @param maxAttempts 最大重试次数
     * @param <T>         第一个参数类型
     * @param <U>         第二个参数类型
     * @return 带重试的 Try
     */
    public static <T, U> Try<Void> retry(CheckedBiConsumer<T, U> consumer, T t, U u, int maxAttempts) {
        return retry(consumer, t, u, maxAttempts, 0);
    }

    /**
     * 带重试和延迟的 CheckedBiConsumer
     *
     * @param consumer    原始双参数消费者
     * @param t           第一个参数
     * @param u           第二个参数
     * @param maxAttempts 最大重试次数
     * @param delayMillis 延迟时间（毫秒）
     * @param <T>         第一个参数类型
     * @param <U>         第二个参数类型
     * @return 带重试的 Try
     */
    public static <T, U> Try<Void> retry(CheckedBiConsumer<T, U> consumer, T t, U u, int maxAttempts, long delayMillis) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }

        Try<Void> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<Void> result = Try.ofConsumer(t, u, consumer);

            if (result.isSuccess()) {
                return result;
            }

            lastFailure = result;

            if (attempt < maxAttempts && delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Try.failure(e);
                }
            }
        }

        return lastFailure;
    }

    /**
     * 带条件重试的 CheckedBiConsumer
     *
     * @param consumer    原始双参数消费者
     * @param t           第一个参数
     * @param u           第二个参数
     * @param maxAttempts 最大重试次数
     * @param shouldRetry 重试条件
     * @param <T>         第一个参数类型
     * @param <U>         第二个参数类型
     * @return 带重试的 Try
     */
    public static <T, U> Try<Void> retry(CheckedBiConsumer<T, U> consumer, T t, U u, int maxAttempts,
                                         java.util.function.Function<Throwable, Boolean> shouldRetry) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(shouldRetry, "shouldRetry cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Try<Void> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<Void> result = Try.ofConsumer(t, u, consumer);

            if (result.isSuccess()) {
                return result;
            }

            lastFailure = result;

            if (attempt < maxAttempts && shouldRetry.apply(result.getCause())) {
                continue;
            } else {
                break;
            }
        }

        return lastFailure;
    }

    // ==================== 超时方法 ====================

    /**
     * 带超时的 CheckedBiConsumer
     *
     * @param consumer      原始双参数消费者
     * @param t             第一个参数
     * @param u             第二个参数
     * @param timeoutMillis 超时时间（毫秒）
     * @param <T>           第一个参数类型
     * @param <U>           第二个参数类型
     * @return 带超时的 Try
     */
    public static <T, U> Try<Void> timeout(CheckedBiConsumer<T, U> consumer, T t, U u, long timeoutMillis) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }

        return Try.of(() -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> execute(consumer, t, u));

            try {
                future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
                return null;
            } catch (java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMillis + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== 装饰器方法 ====================

    /**
     * 创建带参数验证的 CheckedBiConsumer
     *
     * @param consumer  原始双参数消费者
     * @param validator 参数验证器
     * @param <T>       第一个参数类型
     * @param <U>       第二个参数类型
     * @return 带验证的 CheckedBiConsumer
     */
    public static <T, U> CheckedBiConsumer<T, U> validation(CheckedBiConsumer<T, U> consumer,
                                                            BiPredicate<T, U> validator) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");

        return (t, u) -> {
            if (!validator.test(t, u)) {
                throw new IllegalArgumentException("Validation failed for parameters: (" + t + ", " + u + ")");
            }
            consumer.accept(t, u);
        };
    }

    /**
     * 创建带异常处理的 CheckedBiConsumer
     *
     * @param consumer         原始双参数消费者
     * @param exceptionHandler 异常处理消费者
     * @param <T>              第一个参数类型
     * @param <U>              第二个参数类型
     * @return 带异常处理的 CheckedBiConsumer
     */
    public static <T, U> CheckedBiConsumer<T, U> exceptionHandling(CheckedBiConsumer<T, U> consumer,
                                                                   BiConsumer<T, U> exceptionHandler) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");

        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Exception e) {
                exceptionHandler.accept(t, u);
            }
        };
    }

    /**
     * 创建带降级处理的 CheckedBiConsumer
     *
     * @param primary  主要双参数消费者
     * @param fallback 降级双参数消费者
     * @param <T>      第一个参数类型
     * @param <U>      第二个参数类型
     * @return 带降级的 CheckedBiConsumer
     */
    public static <T, U> CheckedBiConsumer<T, U> fallback(CheckedBiConsumer<T, U> primary,
                                                          CheckedBiConsumer<T, U> fallback) {
        Objects.requireNonNull(primary, "primary consumer cannot be null");
        Objects.requireNonNull(fallback, "fallback consumer cannot be null");

        return (t, u) -> {
            try {
                primary.accept(t, u);
            } catch (Exception e) {
                fallback.accept(t, u);
            }
        };
    }


    // ==================== 工具方法 ====================

    /**
     * 创建空操作消费者
     *
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 空操作消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> noop() {
        return CheckedBiConsumer.noop();
    }

    /**
     * 创建打印消费者
     *
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 打印消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> println() {
        return (t, u) -> System.out.println("(" + t + ", " + u + ")");
    }

    /**
     * 创建格式化打印消费者
     *
     * @param format 格式字符串
     * @param <T>    第一个参数类型
     * @param <U>    第二个参数类型
     * @return 格式化打印消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> printf(String format) {
        Objects.requireNonNull(format, "format cannot be null");
        return (t, u) -> System.out.printf(format, t, u);
    }

    /**
     * 创建异常消费者
     *
     * @param exception 要抛出的异常
     * @param <T>       第一个参数类型
     * @param <U>       第二个参数类型
     * @return 异常消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return (t, u) -> {
            throw exception;
        };
    }

    /**
     * 组合多个消费者
     *
     * @param consumers 消费者数组
     * @param <T>       第一个参数类型
     * @param <U>       第二个参数类型
     * @return 组合后的消费者
     */
    @SafeVarargs
    public static <T, U> CheckedBiConsumer<T, U> compose(CheckedBiConsumer<T, U>... consumers) {
        Objects.requireNonNull(consumers, "consumers cannot be null");
        if (consumers.length == 0) {
            return noop();
        }

        return (t, u) -> {
            for (CheckedBiConsumer<T, U> consumer : consumers) {
                if (consumer != null) {
                    consumer.accept(t, u);
                }
            }
        };
    }

    /**
     * 条件执行消费者
     *
     * @param condition 条件判断
     * @param consumer  消费者
     * @param <T>       第一个参数类型
     * @param <U>       第二个参数类型
     * @return 条件消费者
     */
    public static <T, U> CheckedBiConsumer<T, U> conditional(BiPredicate<T, U> condition,
                                                             CheckedBiConsumer<T, U> consumer) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");

        return (t, u) -> {
            if (condition.test(t, u)) {
                consumer.accept(t, u);
            }
        };
    }
}
