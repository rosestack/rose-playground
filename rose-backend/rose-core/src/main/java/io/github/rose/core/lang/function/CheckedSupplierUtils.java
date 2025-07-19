package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedSupplier;
import io.github.rose.core.lang.function.core.Try;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * CheckedSupplier 工具类
 * 提供基于 CheckedSupplier 的便捷方法
 *
 * @author rose
 */
public final class CheckedSupplierUtils {

    private CheckedSupplierUtils() {
        // 工具类，禁止实例化
    }

    // ==================== 转换方法 ====================

    /**
     * 将 CheckedSupplier 转换为标准 Supplier
     *
     * @param checkedSupplier 受检异常的供应者
     * @param <T>             返回类型
     * @return 标准 Supplier
     */
    public static <T> Supplier<T> unchecked(CheckedSupplier<T> checkedSupplier) {
        Objects.requireNonNull(checkedSupplier, "checkedSupplier cannot be null");
        return checkedSupplier.unchecked();
    }

    /**
     * 将标准 Supplier 转换为 CheckedSupplier
     *
     * @param supplier 标准供应者
     * @param <T>      返回类型
     * @return 受检异常的供应者
     */
    public static <T> CheckedSupplier<T> checked(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return CheckedSupplier.from(supplier);
    }

    // ==================== 重试方法 ====================

    /**
     * 带重试的 CheckedSupplier
     *
     * @param supplier    原始供应者
     * @param maxAttempts 最大重试次数
     * @param <T>         返回类型
     * @return 带重试的 Try
     */
    public static <T> Try<T> retry(CheckedSupplier<T> supplier, int maxAttempts) {
        return retry(supplier, maxAttempts, 0);
    }

    /**
     * 带重试和延迟的 CheckedSupplier
     *
     * @param supplier    原始供应者
     * @param maxAttempts 最大重试次数
     * @param delayMillis 延迟时间（毫秒）
     * @param <T>         返回类型
     * @return 带重试的 Try
     */
    public static <T> Try<T> retry(CheckedSupplier<T> supplier, int maxAttempts, long delayMillis) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }

        Try<T> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<T> result = Try.ofSupplier(supplier);

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
     * 带条件重试的 CheckedSupplier
     *
     * @param supplier    原始供应者
     * @param maxAttempts 最大重试次数
     * @param shouldRetry 重试条件
     * @param <T>         返回类型
     * @return 带重试的 Try
     */
    public static <T> Try<T> retry(CheckedSupplier<T> supplier, int maxAttempts,
                                   java.util.function.Function<Throwable, Boolean> shouldRetry) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(shouldRetry, "shouldRetry cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Try<T> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<T> result = Try.ofSupplier(supplier);

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
     * 带超时的 CheckedSupplier
     *
     * @param supplier      原始供应者
     * @param timeoutMillis 超时时间（毫秒）
     * @param <T>           返回类型
     * @return 带超时的 Try
     */
    public static <T> Try<T> timeout(CheckedSupplier<T> supplier, long timeoutMillis) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }

        return Try.of(() -> {
            java.util.concurrent.CompletableFuture<T> future = java.util.concurrent.CompletableFuture.supplyAsync(
                    supplier.unchecked()
            );

            try {
                return future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMillis + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== 工具方法 ====================

    /**
     * 创建常量供应者
     *
     * @param value 常量值
     * @param <T>   值类型
     * @return 常量供应者
     */
    public static <T> CheckedSupplier<T> constant(T value) {
        return CheckedSupplier.constant(value);
    }

    /**
     * 创建空值供应者
     *
     * @param <T> 值类型
     * @return 空值供应者
     */
    public static <T> CheckedSupplier<T> empty() {
        return () -> null;
    }

    /**
     * 创建异常供应者
     *
     * @param exception 要抛出的异常
     * @param <T>       值类型
     * @return 异常供应者
     */
    public static <T> CheckedSupplier<T> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return () -> {
            throw exception;
        };
    }
} 