package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedBiSupplier;
import io.github.rose.core.lang.function.core.Try;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * CheckedBiSupplier 工具类
 * 提供基于 CheckedBiSupplier 的便捷方法
 *
 * @author rose
 */
public final class CheckedBiSupplierUtils {

    private CheckedBiSupplierUtils() {
        // 工具类，禁止实例化
    }

    // ==================== 转换方法 ====================

    /**
     * 将 CheckedBiSupplier 转换为标准 BiFunction
     *
     * @param checkedSupplier 受检异常的双参数供应者
     * @param <T>             输入参数类型
     * @param <U>             返回类型
     * @return 标准 BiFunction
     */
    public static <T, U> BiFunction<T, Void, U> unchecked(CheckedBiSupplier<T, U> checkedSupplier) {
        Objects.requireNonNull(checkedSupplier, "checkedSupplier cannot be null");
        return checkedSupplier.unchecked();
    }

    /**
     * 将标准 BiFunction 转换为 CheckedBiSupplier
     *
     * @param function 标准双参数函数
     * @param <T>      输入参数类型
     * @param <U>      返回类型
     * @return 受检异常的双参数供应者
     */
    public static <T, U> CheckedBiSupplier<T, U> checked(BiFunction<T, Void, U> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return CheckedBiSupplier.from(function);
    }

    // ==================== 执行方法 ====================

    /**
     * 执行受检异常的双参数供应者
     *
     * @param supplier 受检异常的双参数供应者
     * @param t        输入参数
     * @param <T>      输入参数类型
     * @param <U>      返回类型
     * @return 执行结果
     */
    public static <T, U> U execute(CheckedBiSupplier<T, U> supplier, T t) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return supplier.get(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行受检异常的双参数供应者，忽略异常
     *
     * @param supplier     受检异常的双参数供应者
     * @param t            输入参数
     * @param defaultValue 默认值
     * @param <T>          输入参数类型
     * @param <U>          返回类型
     * @return 执行结果或默认值
     */
    public static <T, U> U executeSilently(CheckedBiSupplier<T, U> supplier, T t, U defaultValue) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            U result = supplier.get(t);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== 异步执行方法 ====================

    /**
     * 异步执行受检异常的双参数供应者
     *
     * @param supplier 受检异常的双参数供应者
     * @param t        输入参数
     * @param <T>      输入参数类型
     * @param <U>      返回类型
     * @return CompletableFuture
     */
    public static <T, U> CompletableFuture<U> async(CheckedBiSupplier<T, U> supplier, T t) {
        return async(supplier, t, null);
    }

    /**
     * 异步执行受检异常的双参数供应者
     *
     * @param supplier 受检异常的双参数供应者
     * @param t        输入参数
     * @param executor 执行器
     * @param <T>      输入参数类型
     * @param <U>      返回类型
     * @return CompletableFuture
     */
    public static <T, U> CompletableFuture<U> async(CheckedBiSupplier<T, U> supplier, T t, Executor executor) {
        Objects.requireNonNull(supplier, "supplier cannot be null");

        if (executor != null) {
            return CompletableFuture.supplyAsync(() -> execute(supplier, t), executor);
        } else {
            return CompletableFuture.supplyAsync(() -> execute(supplier, t));
        }
    }

    // ==================== 批量处理方法 ====================

    /**
     * 遍历输入参数集合
     *
     * @param supplier   受检异常的双参数供应者
     * @param collection 输入参数的集合
     * @param <T>        输入参数类型
     * @param <U>        返回类型
     * @return 结果列表
     */
    public static <T, U> java.util.List<U> forEach(CheckedBiSupplier<T, U> supplier, java.util.Collection<T> collection) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        java.util.List<U> results = new java.util.ArrayList<>();
        for (T t : collection) {
            results.add(execute(supplier, t));
        }
        return results;
    }

    /**
     * 遍历输入参数集合，收集所有结果（包括失败）
     *
     * @param supplier   受检异常的双参数供应者
     * @param collection 输入参数的集合
     * @param <T>        输入参数类型
     * @param <U>        返回类型
     * @return Try 结果列表
     */
    public static <T, U> java.util.List<Try<U>> forEachCollect(CheckedBiSupplier<T, U> supplier, java.util.Collection<T> collection) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        return collection.stream()
                .map(t -> Try.ofSupplier(t, supplier))
                .collect(java.util.stream.Collectors.toList());
    }

    // ==================== 重试方法 ====================

    /**
     * 带重试的 CheckedBiSupplier
     *
     * @param supplier    原始双参数供应者
     * @param t           输入参数
     * @param maxAttempts 最大重试次数
     * @param <T>         输入参数类型
     * @param <U>         返回类型
     * @return 带重试的 Try
     */
    public static <T, U> Try<U> retry(CheckedBiSupplier<T, U> supplier, T t, int maxAttempts) {
        return retry(supplier, t, maxAttempts, 0);
    }

    /**
     * 带重试和延迟的 CheckedBiSupplier
     *
     * @param supplier    原始双参数供应者
     * @param t           输入参数
     * @param maxAttempts 最大重试次数
     * @param delayMillis 延迟时间（毫秒）
     * @param <T>         输入参数类型
     * @param <U>         返回类型
     * @return 带重试的 Try
     */
    public static <T, U> Try<U> retry(CheckedBiSupplier<T, U> supplier, T t, int maxAttempts, long delayMillis) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }

        Try<U> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<U> result = Try.ofSupplier(t, supplier);

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
     * 带条件重试的 CheckedBiSupplier
     *
     * @param supplier    原始双参数供应者
     * @param t           输入参数
     * @param maxAttempts 最大重试次数
     * @param shouldRetry 重试条件
     * @param <T>         输入参数类型
     * @param <U>         返回类型
     * @return 带重试的 Try
     */
    public static <T, U> Try<U> retry(CheckedBiSupplier<T, U> supplier, T t, int maxAttempts,
                                      java.util.function.Function<Throwable, Boolean> shouldRetry) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(shouldRetry, "shouldRetry cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Try<U> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<U> result = Try.ofSupplier(t, supplier);

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
     * 带超时的 CheckedBiSupplier
     *
     * @param supplier      原始双参数供应者
     * @param t             输入参数
     * @param timeoutMillis 超时时间（毫秒）
     * @param <T>           输入参数类型
     * @param <U>           返回类型
     * @return 带超时的 Try
     */
    public static <T, U> Try<U> timeout(CheckedBiSupplier<T, U> supplier, T t, long timeoutMillis) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }

        return Try.of(() -> {
            CompletableFuture<U> future = CompletableFuture.supplyAsync(() -> execute(supplier, t));

            try {
                return future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMillis + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== 装饰器方法 ====================

    /**
     * 创建带参数验证的 CheckedBiSupplier
     *
     * @param supplier  原始双参数供应者
     * @param validator 参数验证器
     * @param <T>       输入参数类型
     * @param <U>       返回类型
     * @return 带验证的 CheckedBiSupplier
     */
    public static <T, U> CheckedBiSupplier<T, U> validation(CheckedBiSupplier<T, U> supplier,
                                                            Predicate<T> validator) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");

        return t -> {
            if (!validator.test(t)) {
                throw new IllegalArgumentException("Validation failed for parameter: " + t);
            }
            return supplier.get(t);
        };
    }

    /**
     * 创建带结果验证的 CheckedBiSupplier
     *
     * @param supplier        原始双参数供应者
     * @param resultValidator 结果验证器
     * @param <T>             输入参数类型
     * @param <U>             返回类型
     * @return 带结果验证的 CheckedBiSupplier
     */
    public static <T, U> CheckedBiSupplier<T, U> withResultValidation(CheckedBiSupplier<T, U> supplier,
                                                                      Predicate<U> resultValidator) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(resultValidator, "resultValidator cannot be null");

        return t -> {
            U result = supplier.get(t);
            if (!resultValidator.test(result)) {
                throw new IllegalArgumentException("Result validation failed for result: " + result);
            }
            return result;
        };
    }

    /**
     * 创建带异常处理的 CheckedBiSupplier
     *
     * @param supplier         原始双参数供应者
     * @param exceptionHandler 异常处理函数
     * @param <T>              输入参数类型
     * @param <U>              返回类型
     * @return 带异常处理的 CheckedBiSupplier
     */
    public static <T, U> CheckedBiSupplier<T, U> exceptionHandling(CheckedBiSupplier<T, U> supplier,
                                                                   java.util.function.Function<Exception, U> exceptionHandler) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");

        return t -> {
            try {
                return supplier.get(t);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }

    /**
     * 创建带降级处理的 CheckedBiSupplier
     *
     * @param primary  主要双参数供应者
     * @param fallback 降级双参数供应者
     * @param <T>      输入参数类型
     * @param <U>      返回类型
     * @return 带降级的 CheckedBiSupplier
     */
    public static <T, U> CheckedBiSupplier<T, U> fallback(CheckedBiSupplier<T, U> primary,
                                                          CheckedBiSupplier<T, U> fallback) {
        Objects.requireNonNull(primary, "primary supplier cannot be null");
        Objects.requireNonNull(fallback, "fallback supplier cannot be null");

        return t -> {
            try {
                return primary.get(t);
            } catch (Exception e) {
                return fallback.get(t);
            }
        };
    }


    // ==================== 工具方法 ====================

    /**
     * 创建常量供应者
     *
     * @param value 常量值
     * @param <T>   输入参数类型
     * @param <U>   返回类型
     * @return 常量供应者
     */
    public static <T, U> CheckedBiSupplier<T, U> constant(U value) {
        return CheckedBiSupplier.constant(value);
    }

    /**
     * 创建空值供应者
     *
     * @param <T> 输入参数类型
     * @param <U> 返回类型
     * @return 空值供应者
     */
    public static <T, U> CheckedBiSupplier<T, U> empty() {
        return t -> null;
    }

    /**
     * 创建异常供应者
     *
     * @param exception 要抛出的异常
     * @param <T>       输入参数类型
     * @param <U>       返回类型
     * @return 异常供应者
     */
    public static <T, U> CheckedBiSupplier<T, U> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return t -> {
            throw exception;
        };
    }

    /**
     * 条件执行供应者
     *
     * @param condition    条件判断
     * @param supplier     供应者
     * @param defaultValue 默认值
     * @param <T>          输入参数类型
     * @param <U>          返回类型
     * @return 条件供应者
     */
    public static <T, U> CheckedBiSupplier<T, U> conditional(Predicate<T> condition,
                                                             CheckedBiSupplier<T, U> supplier,
                                                             U defaultValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(supplier, "supplier cannot be null");

        return t -> {
            if (condition.test(t)) {
                return supplier.get(t);
            } else {
                return defaultValue;
            }
        };
    }
}
