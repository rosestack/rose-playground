package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedBiFunction;
import io.github.rose.core.lang.function.core.Try;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * CheckedBiFunction 工具类
 * 提供基于 CheckedBiFunction 的便捷方法
 * 
 * @author rose
 */
public final class CheckedBiFunctionUtils {
    
    private CheckedBiFunctionUtils() {
        // 工具类，禁止实例化
    }
    
    /**
     * 从受检异常的双参数函数创建 Option
     *
     * @param function 受检异常的双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Option 结果
     */
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 CheckedBiFunction 转换为标准 BiFunction
     * 
     * @param checkedFunction 受检异常的双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 标准 BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> unchecked(CheckedBiFunction<T, U, R> checkedFunction) {
        Objects.requireNonNull(checkedFunction, "checkedFunction cannot be null");
        return checkedFunction.unchecked();
    }
    
    /**
     * 将标准 BiFunction 转换为 CheckedBiFunction
     * 
     * @param function 标准双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 受检异常的双参数函数
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> checked(BiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return CheckedBiFunction.from(function);
    }
    
    // ==================== 执行方法 ====================
    
    /**
     * 执行受检异常的双参数函数
     *
     * @param function 受检异常的双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 执行结果
     */
    public static <T, U, R> R execute(CheckedBiFunction<T, U, R> function, T t, U u) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return function.apply(t, u);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行受检异常的双参数函数，忽略异常
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param defaultValue 默认值
     * @param function 受检异常的双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 执行结果或默认值
     */
    public static <T, U, R> R executeSilently(T t, U u, R defaultValue, CheckedBiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            R result = function.apply(t, u);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    // ==================== 异步执行方法 ====================
    
    /**
     * 异步执行受检异常的双参数函数
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param function 受检异常的双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return CompletableFuture
     */
    public static <T, U, R> CompletableFuture<R> async(T t, U u, CheckedBiFunction<T, U, R> function) {
        return async(t, u, function, null);
    }

    /**
     * 异步执行受检异常的双参数函数
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param function 受检异常的双参数函数
     * @param executor 执行器
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return CompletableFuture
     */
    public static <T, U, R> CompletableFuture<R> async(T t, U u, CheckedBiFunction<T, U, R> function, Executor executor) {
        Objects.requireNonNull(function, "function cannot be null");

        if (executor != null) {
            return CompletableFuture.supplyAsync(() -> execute(function, t, u), executor);
        } else {
            return CompletableFuture.supplyAsync(() -> execute(function, t, u));
        }
    }
    
    // ==================== 重试方法 ====================
    
    /**
     * 带重试的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param maxAttempts 最大重试次数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带重试的 Try
     */
    public static <T, U, R> Try<R> retry(CheckedBiFunction<T, U, R> function, T t, U u, int maxAttempts) {
        return retry(function, t, u, maxAttempts, 0);
    }

    /**
     * 带重试和延迟的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param maxAttempts 最大重试次数
     * @param delayMillis 延迟时间（毫秒）
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带重试的 Try
     */
    public static <T, U, R> Try<R> retry(CheckedBiFunction<T, U, R> function, T t, U u, int maxAttempts, long delayMillis) {
        Objects.requireNonNull(function, "function cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }

        Try<R> lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<R> result = Try.ofBiFunction(t, u, function);

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
     * 带条件重试的 CheckedBiFunction
     * 
     * @param function 原始双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param maxAttempts 最大重试次数
     * @param shouldRetry 重试条件
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带重试的 Try
     */
    public static <T, U, R> Try<R> retry(CheckedBiFunction<T, U, R> function, T t, U u, int maxAttempts,
                                        java.util.function.Function<Throwable, Boolean> shouldRetry) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(shouldRetry, "shouldRetry cannot be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        
        Try<R> lastFailure = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Try<R> result = Try.ofBiFunction(t, u, function);
            
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
     * 带超时的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param timeoutMillis 超时时间（毫秒）
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带超时的 Try
     */
    public static <T, U, R> Try<R> timeout(CheckedBiFunction<T, U, R> function, T t, U u, long timeoutMillis) {
        Objects.requireNonNull(function, "function cannot be null");
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be positive");
        }

        return Try.of(() -> {
            CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> execute(function, t, u));

            try {
                return future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMillis + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== 批量处理方法 ====================

    /**
     * 固定第一个参数，映射第二个参数集合
     *
     * @param function 受检异常的双参数函数
     * @param t 固定的第一个参数
     * @param collection 第二个参数的集合
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 结果列表
     */
    public static <T, U, R> java.util.List<R> forEachWithFirst(CheckedBiFunction<T, U, R> function, T t, java.util.Collection<U> collection) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        java.util.List<R> results = new java.util.ArrayList<>();
        for (U u : collection) {
            results.add(execute(function, t, u));
        }
        return results;
    }

    /**
     * 固定第二个参数，映射第一个参数集合
     *
     * @param function 受检异常的双参数函数
     * @param collection 第一个参数的集合
     * @param u 固定的第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 结果列表
     */
    public static <T, U, R> java.util.List<R> forEachWithSecond(CheckedBiFunction<T, U, R> function, java.util.Collection<T> collection, U u) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        java.util.List<R> results = new java.util.ArrayList<>();
        for (T t : collection) {
            results.add(execute(function, t, u));
        }
        return results;
    }

    /**
     * 固定第一个参数，映射第二个参数集合，收集所有结果（包括失败）
     *
     * @param function 受检异常的双参数函数
     * @param t 固定的第一个参数
     * @param collection 第二个参数的集合
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果列表
     */
    public static <T, U, R> java.util.List<Try<R>> forEachWithFirstCollect(CheckedBiFunction<T, U, R> function, T t, java.util.Collection<U> collection) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        return collection.stream()
                .map(u -> Try.ofBiFunction(t, u, function))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 固定第二个参数，映射第一个参数集合，收集所有结果（包括失败）
     *
     * @param function 受检异常的双参数函数
     * @param collection 第一个参数的集合
     * @param u 固定的第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果列表
     */
    public static <T, U, R> java.util.List<Try<R>> forEachWithSecondCollect(CheckedBiFunction<T, U, R> function, java.util.Collection<T> collection, U u) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");

        return collection.stream()
                .map(t -> Try.ofBiFunction(t, u, function))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 应用到 Map 的每个键值对
     *
     * @param function 受检异常的双参数函数
     * @param map Map 集合
     * @param <T> 键类型
     * @param <U> 值类型
     * @param <R> 返回类型
     * @return 结果列表
     */
    public static <T, U, R> java.util.List<R> forEachMap(CheckedBiFunction<T, U, R> function, java.util.Map<T, U> map) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(map, "map cannot be null");

        java.util.List<R> results = new java.util.ArrayList<>();
        for (java.util.Map.Entry<T, U> entry : map.entrySet()) {
            results.add(execute(function, entry.getKey(), entry.getValue()));
        }
        return results;
    }

    // ==================== 日志方法 ====================

    /**
     * 带日志的 CheckedBiFunction
     *
     * @param function 受检异常的双参数函数
     * @param t 第一个参数
     * @param u 第二个参数
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return Try 结果
     */
    public static <T, U, R> Try<R> logged(CheckedBiFunction<T, U, R> function, T t, U u, String operationName) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");

        System.out.println("Starting CheckedBiFunction operation: " + operationName + " with values: (" + t + ", " + u + ")");

        Try<R> result = Try.ofBiFunction(t, u, function);

        if (result.isSuccess()) {
            System.out.println("CheckedBiFunction operation " + operationName + " completed successfully with result: " + result.get());
        } else {
            System.err.println("CheckedBiFunction operation " + operationName + " failed: " + result.getCause().getMessage());
        }

        return result;
    }

    // ==================== 工具方法 ====================

    /**
     * 创建身份函数（返回第一个参数）
     *
     * @param <T> 类型
     * @return 身份函数
     */
    public static <T> CheckedBiFunction<T, T, T> identity() {
        return (t, u) -> t;
    }

    /**
     * 创建常量函数
     *
     * @param value 常量值
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 常量函数
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> constant(R value) {
        return (t, u) -> value;
    }

    /**
     * 创建空值函数
     *
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 空值函数
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> empty() {
        return (t, u) -> null;
    }

    /**
     * 创建异常函数
     *
     * @param exception 要抛出的异常
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 异常函数
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> failure(Exception exception) {
        Objects.requireNonNull(exception, "exception cannot be null");
        return (t, u) -> {
            throw exception;
        };
    }

    // ==================== 装饰器方法 ====================

    /**
     * 创建带参数验证的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param validator 参数验证器
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带验证的 CheckedBiFunction
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> validation(CheckedBiFunction<T, U, R> function,
                                                                 BiPredicate<T, U> validator) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");

        return (t, u) -> {
            if (!validator.test(t, u)) {
                throw new IllegalArgumentException("Validation failed for parameters: (" + t + ", " + u + ")");
            }
            return function.apply(t, u);
        };
    }

    /**
     * 创建带结果验证的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param resultValidator 结果验证器
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带结果验证的 CheckedBiFunction
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> resultValidation(CheckedBiFunction<T, U, R> function,
                                                                     java.util.function.Predicate<R> resultValidator) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(resultValidator, "resultValidator cannot be null");

        return (t, u) -> {
            R result = function.apply(t, u);
            if (!resultValidator.test(result)) {
                throw new IllegalArgumentException("Result validation failed for result: " + result);
            }
            return result;
        };
    }

    /**
     * 创建带异常处理的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param exceptionHandler 异常处理函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带异常处理的 CheckedBiFunction
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> exceptionHandling(CheckedBiFunction<T, U, R> function,
                                                                      java.util.function.Function<Exception, R> exceptionHandler) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");

        return (t, u) -> {
            try {
                return function.apply(t, u);
            } catch (Exception e) {
                return exceptionHandler.apply(e);
            }
        };
    }

    /**
     * 创建带降级处理的 CheckedBiFunction
     *
     * @param primary 主要双参数函数
     * @param fallback 降级双参数函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带降级的 CheckedBiFunction
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> fallback(CheckedBiFunction<T, U, R> primary,
                                                               CheckedBiFunction<T, U, R> fallback) {
        Objects.requireNonNull(primary, "primary function cannot be null");
        Objects.requireNonNull(fallback, "fallback function cannot be null");

        return (t, u) -> {
            try {
                return primary.apply(t, u);
            } catch (Exception e) {
                return fallback.apply(t, u);
            }
        };
    }

    /**
     * 创建带指标收集的 CheckedBiFunction
     *
     * @param function 原始双参数函数
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 带指标的 CheckedBiFunction
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> metrics(CheckedBiFunction<T, U, R> function,
                                                              java.util.function.BiConsumer<Long, String> metricsCollector,
                                                              String operationName) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(metricsCollector, "metricsCollector cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");

        return (t, u) -> {
            long startTime = System.currentTimeMillis();
            try {
                R result = function.apply(t, u);
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.accept(duration, operationName + " - SUCCESS");
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.accept(duration, operationName + " - FAILURE");
                throw e;
            }
        };
    }



    // ==================== 组合方法 ====================

    /**
     * 组合多个函数（链式调用）
     *
     * @param functions 函数数组
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 组合后的函数
     */
    @SafeVarargs
    public static <T, U, R> CheckedBiFunction<T, U, R> compose(CheckedBiFunction<T, U, R>... functions) {
        Objects.requireNonNull(functions, "functions cannot be null");
        if (functions.length == 0) {
            throw new IllegalArgumentException("functions array cannot be empty");
        }
        if (functions.length == 1) {
            return functions[0];
        }

        return (t, u) -> {
            R result = functions[0].apply(t, u);
            for (int i = 1; i < functions.length; i++) {
                if (functions[i] != null) {
                    // 这里简化处理，实际可能需要更复杂的组合逻辑
                    result = functions[i].apply(t, u);
                }
            }
            return result;
        };
    }

    /**
     * 条件执行函数
     *
     * @param condition 条件判断
     * @param function 函数
     * @param defaultValue 默认值
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 条件函数
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> conditional(BiPredicate<T, U> condition,
                                                                  CheckedBiFunction<T, U, R> function,
                                                                  R defaultValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(function, "function cannot be null");

        return (t, u) -> {
            if (condition.test(t, u)) {
                return function.apply(t, u);
            } else {
                return defaultValue;
            }
        };
    }

    /**
     * 条件执行函数（带默认函数）
     *
     * @param condition 条件判断
     * @param trueFunction 条件为真时执行的函数
     * @param falseFunction 条件为假时执行的函数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回类型
     * @return 条件函数
     */
    public static <T, U, R> CheckedBiFunction<T, U, R> conditional(BiPredicate<T, U> condition,
                                                                  CheckedBiFunction<T, U, R> trueFunction,
                                                                  CheckedBiFunction<T, U, R> falseFunction) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(trueFunction, "trueFunction cannot be null");
        Objects.requireNonNull(falseFunction, "falseFunction cannot be null");

        return (t, u) -> {
            if (condition.test(t, u)) {
                return trueFunction.apply(t, u);
            } else {
                return falseFunction.apply(t, u);
            }
        };
    }

    // ==================== 特殊工具方法 ====================

    /**
     * 创建格式化字符串函数
     *
     * @param format 格式字符串
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 格式化函数
     */
    public static <T, U> CheckedBiFunction<T, U, String> formatter(String format) {
        Objects.requireNonNull(format, "format cannot be null");
        return (t, u) -> String.format(format, t, u);
    }

    /**
     * 创建字符串连接函数
     *
     * @param separator 分隔符
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 字符串连接函数
     */
    public static <T, U> CheckedBiFunction<T, U, String> joiner(String separator) {
        Objects.requireNonNull(separator, "separator cannot be null");
        return (t, u) -> String.valueOf(t) + separator + String.valueOf(u);
    }

    /**
     * 创建求和函数（适用于数字类型）
     *
     * @param <T> 数字类型
     * @return 求和函数
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> CheckedBiFunction<T, T, Double> sum() {
        return (t, u) -> t.doubleValue() + u.doubleValue();
    }

    /**
     * 创建求积函数（适用于数字类型）
     *
     * @param <T> 数字类型
     * @return 求积函数
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> CheckedBiFunction<T, T, Double> multiply() {
        return (t, u) -> t.doubleValue() * u.doubleValue();
    }

    /**
     * 创建比较函数
     *
     * @param <T> 可比较类型
     * @return 比较函数（返回比较结果）
     */
    public static <T extends Comparable<T>> CheckedBiFunction<T, T, Integer> comparator() {
        return (t, u) -> t.compareTo(u);
    }
}
