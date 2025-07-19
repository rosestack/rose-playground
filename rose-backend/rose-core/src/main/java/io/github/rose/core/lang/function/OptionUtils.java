package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.core.Option;
import io.github.rose.core.lang.function.core.Try;
import io.github.rose.core.lang.function.core.Either;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Option 工具类
 * 提供基于 Option 的便捷方法
 * 
 * @author rose
 */
public final class OptionUtils {
    
    private OptionUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 创建方法 ====================
    
    /**
     * 从可能为 null 的值创建 Option
     * 
     * @param value 可能为 null 的值
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> of(T value) {
        return value != null ? Option.some(value) : Option.none();
    }
    
    /**
     * 从可能抛出异常的 Supplier 创建 Option
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> ofSupplier(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            T value = supplier.get();
            return value != null ? Option.some(value) : Option.none();
        } catch (Exception e) {
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
        Objects.requireNonNull(function, "function cannot be null");
        try {
            R result = function.apply(value);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            return Option.none();
        }
    }
    
    /**
     * 从条件创建 Option
     * 
     * @param condition 条件
     * @param value 条件为真时的值
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> ofCondition(boolean condition, T value) {
        return condition ? Option.some(value) : Option.none();
    }
    
    /**
     * 从条件和供应者创建 Option
     * 
     * @param condition 条件
     * @param supplier 条件为真时的值供应者
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> ofCondition(boolean condition, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return condition ? of(supplier.get()) : Option.none();
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 Try 转换为 Option
     * 
     * @param tryValue Try 值
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> fromTry(Try<T> tryValue) {
        Objects.requireNonNull(tryValue, "tryValue cannot be null");
        return tryValue.isSuccess() ? Option.some(tryValue.get()) : Option.none();
    }
    
    /**
     * 将 Option 转换为 Try
     * 
     * @param option Option 值
     * @param exception 当 Option 为空时的异常
     * @param <T> 值类型
     * @return Try
     */
    public static <T> Try<T> toTry(Option<T> option, Exception exception) {
        Objects.requireNonNull(option, "option cannot be null");
        Objects.requireNonNull(exception, "exception cannot be null");
        return option.isPresent() ? Try.success(option.get()) : Try.failure(exception);
    }
    
    /**
     * 将 Either 转换为 Option
     * 
     * @param either Either 值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Option
     */
    public static <L, R> Option<R> fromEither(Either<L, R> either) {
        Objects.requireNonNull(either, "either cannot be null");
        return either.isRight() ? Option.some(either.getRight()) : Option.none();
    }
    
    /**
     * 将 Option 转换为 Either
     * 
     * @param option Option 值
     * @param leftValue 当 Option 为空时的左值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Either
     */
    public static <L, R> Either<L, R> toEither(Option<R> option, L leftValue) {
        Objects.requireNonNull(option, "option cannot be null");
        return option.isPresent() ? Either.right(option.get()) : Either.left(leftValue);
    }
    
    // ==================== 执行方法 ====================
    
    /**
     * 执行 Option，返回值或抛出异常
     * 
     * @param option Option 值
     * @param exception 当 Option 为空时的异常
     * @param <T> 值类型
     * @return 值
     * @throws RuntimeException 如果 Option 为空
     */
    public static <T> T execute(Option<T> option, Exception exception) {
        Objects.requireNonNull(option, "option cannot be null");
        Objects.requireNonNull(exception, "exception cannot be null");
        if (option.isPresent()) {
            return option.get();
        } else {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * 执行 Option，返回值或默认值
     * 
     * @param option Option 值
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 值或默认值
     */
    public static <T> T getOrElse(Option<T> option, T defaultValue) {
        Objects.requireNonNull(option, "option cannot be null");
        return option.isPresent() ? option.get() : defaultValue;
    }
    
    /**
     * 执行 Option，返回值或通过供应者获取默认值
     * 
     * @param option Option 值
     * @param defaultSupplier 默认值供应者
     * @param <T> 值类型
     * @return 值或默认值
     */
    public static <T> T getOrElse(Option<T> option, Supplier<T> defaultSupplier) {
        Objects.requireNonNull(option, "option cannot be null");
        Objects.requireNonNull(defaultSupplier, "defaultSupplier cannot be null");
        return option.isPresent() ? option.get() : defaultSupplier.get();
    }
    
    // ==================== 异步执行方法 ====================
    
    /**
     * 异步执行可能抛出异常的 Supplier
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 值类型
     * @return CompletableFuture<Option>
     */
    public static <T> CompletableFuture<Option<T>> async(Supplier<T> supplier) {
        return async(supplier, null);
    }
    
    /**
     * 异步执行可能抛出异常的 Supplier
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param executor 执行器
     * @param <T> 值类型
     * @return CompletableFuture<Option>
     */
    public static <T> CompletableFuture<Option<T>> async(Supplier<T> supplier, Executor executor) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        
        if (executor != null) {
            return CompletableFuture.supplyAsync(() -> ofSupplier(supplier), executor);
        } else {
            return CompletableFuture.supplyAsync(() -> ofSupplier(supplier));
        }
    }
    
    // ==================== 批量处理方法 ====================
    
    /**
     * 遍历集合，收集所有结果
     * 
     * @param collection 输入集合
     * @param function 转换函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return Option 结果列表
     */
    public static <T, R> java.util.List<Option<R>> forEach(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return collection.stream()
                .map(t -> ofFunction(t, function))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 遍历集合，只保留有值的结果
     * 
     * @param collection 输入集合
     * @param function 转换函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 有值结果列表
     */
    public static <T, R> java.util.List<R> forEachDefined(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return collection.stream()
                .map(t -> ofFunction(t, function))
                .filter(Option::isPresent)
                .map(Option::get)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 过滤集合中的 Option，只保留有值的
     * 
     * @param collection Option 集合
     * @param <T> 值类型
     * @return 有值结果列表
     */
    public static <T> java.util.List<T> filterDefined(java.util.Collection<Option<T>> collection) {
        Objects.requireNonNull(collection, "collection cannot be null");
        
        return collection.stream()
                .filter(Option::isPresent)
                .map(Option::get)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建有值的 Option
     * 
     * @param value 值
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> some(T value) {
        return Option.some(value);
    }
    
    /**
     * 创建空的 Option
     * 
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> none() {
        return Option.none();
    }
    
    /**
     * 条件执行 Option
     * 
     * @param condition 条件
     * @param option Option 值
     * @param defaultOption 默认 Option
     * @param <T> 值类型
     * @return Option
     */
    public static <T> Option<T> conditional(boolean condition, Option<T> option, Option<T> defaultOption) {
        return condition ? option : defaultOption;
    }
    
    /**
     * 第一个非空的 Option
     * 
     * @param options Option 数组
     * @param <T> 值类型
     * @return 第一个非空的 Option
     */
    @SafeVarargs
    public static <T> Option<T> firstDefined(Option<T>... options) {
        Objects.requireNonNull(options, "options cannot be null");
        
        for (Option<T> option : options) {
            if (option != null && option.isPresent()) {
                return option;
            }
        }
        return Option.none();
    }
    
    /**
     * 组合多个 Option，只有全部有值时才返回有值的 Option
     * 
     * @param option1 第一个 Option
     * @param option2 第二个 Option
     * @param combiner 组合函数
     * @param <T> 第一个值类型
     * @param <U> 第二个值类型
     * @param <R> 结果类型
     * @return 组合后的 Option
     */
    public static <T, U, R> Option<R> combine(Option<T> option1, Option<U> option2, 
                                             java.util.function.BiFunction<T, U, R> combiner) {
        Objects.requireNonNull(option1, "option1 cannot be null");
        Objects.requireNonNull(option2, "option2 cannot be null");
        Objects.requireNonNull(combiner, "combiner cannot be null");
        
        if (option1.isPresent() && option2.isPresent()) {
            return Option.some(combiner.apply(option1.get(), option2.get()));
        } else {
            return Option.none();
        }
    }
}
