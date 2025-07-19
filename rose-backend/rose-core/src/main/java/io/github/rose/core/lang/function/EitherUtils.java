package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.core.Either;
import io.github.rose.core.lang.function.core.Try;
import io.github.rose.core.lang.function.core.Option;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Either 工具类
 * 提供基于 Either 的便捷方法
 * 
 * @author rose
 */
public final class EitherUtils {
    
    private EitherUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 创建方法 ====================
    
    /**
     * 从可能抛出异常的 Supplier 创建 Either
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 成功类型
     * @return Either
     */
    public static <T> Either<Throwable, T> ofSupplier(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return Either.right(supplier.get());
        } catch (Exception e) {
            return Either.left(e);
        }
    }
    
    /**
     * 从可能抛出异常的函数创建 Either
     * 
     * @param value 输入值
     * @param function 可能抛出异常的函数
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return Either
     */
    public static <T, R> Either<Throwable, R> ofFunction(T value, Function<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return Either.right(function.apply(value));
        } catch (Exception e) {
            return Either.left(e);
        }
    }
    
    /**
     * 从条件创建 Either
     * 
     * @param condition 条件
     * @param rightValue 条件为真时的右值
     * @param leftValue 条件为假时的左值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Either
     */
    public static <L, R> Either<L, R> ofCondition(boolean condition, R rightValue, L leftValue) {
        return condition ? Either.right(rightValue) : Either.left(leftValue);
    }
    
    /**
     * 从条件和供应者创建 Either
     * 
     * @param condition 条件
     * @param rightSupplier 条件为真时的右值供应者
     * @param leftSupplier 条件为假时的左值供应者
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Either
     */
    public static <L, R> Either<L, R> ofCondition(boolean condition, Supplier<R> rightSupplier, Supplier<L> leftSupplier) {
        Objects.requireNonNull(rightSupplier, "rightSupplier cannot be null");
        Objects.requireNonNull(leftSupplier, "leftSupplier cannot be null");
        return condition ? Either.right(rightSupplier.get()) : Either.left(leftSupplier.get());
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 Try 转换为 Either
     * 
     * @param tryValue Try 值
     * @param <T> 值类型
     * @return Either
     */
    public static <T> Either<Throwable, T> fromTry(Try<T> tryValue) {
        Objects.requireNonNull(tryValue, "tryValue cannot be null");
        return tryValue.isSuccess() ? Either.right(tryValue.get()) : Either.left(tryValue.getCause());
    }
    
    /**
     * 将 Either 转换为 Try
     * 
     * @param either Either 值
     * @param <T> 值类型
     * @return Try
     */
    public static <T> Try<T> toTry(Either<Throwable, T> either) {
        Objects.requireNonNull(either, "either cannot be null");
        return either.isRight() ? Try.success(either.getRight()) : Try.failure(either.getLeft());
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
    public static <L, R> Either<L, R> fromOption(Option<R> option, L leftValue) {
        Objects.requireNonNull(option, "option cannot be null");
        return option.isPresent() ? Either.right(option.get()) : Either.left(leftValue);
    }
    
    /**
     * 将 Either 转换为 Option
     * 
     * @param either Either 值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Option
     */
    public static <L, R> Option<R> toOption(Either<L, R> either) {
        Objects.requireNonNull(either, "either cannot be null");
        return either.isRight() ? Option.some(either.getRight()) : Option.none();
    }
    
    // ==================== 执行方法 ====================
    
    /**
     * 执行 Either，返回右值或抛出异常
     * 
     * @param either Either 值
     * @param <T> 值类型
     * @return 右值
     * @throws RuntimeException 如果是左值
     */
    public static <T> T execute(Either<Throwable, T> either) {
        Objects.requireNonNull(either, "either cannot be null");
        if (either.isRight()) {
            return either.getRight();
        } else {
            throw new RuntimeException(either.getLeft());
        }
    }
    
    /**
     * 执行 Either，返回右值或默认值
     * 
     * @param either Either 值
     * @param defaultValue 默认值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return 右值或默认值
     */
    public static <L, R> R getOrElse(Either<L, R> either, R defaultValue) {
        Objects.requireNonNull(either, "either cannot be null");
        return either.isRight() ? either.getRight() : defaultValue;
    }
    
    /**
     * 执行 Either，返回右值或通过供应者获取默认值
     * 
     * @param either Either 值
     * @param defaultSupplier 默认值供应者
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return 右值或默认值
     */
    public static <L, R> R getOrElse(Either<L, R> either, Supplier<R> defaultSupplier) {
        Objects.requireNonNull(either, "either cannot be null");
        Objects.requireNonNull(defaultSupplier, "defaultSupplier cannot be null");
        return either.isRight() ? either.getRight() : defaultSupplier.get();
    }
    
    // ==================== 异步执行方法 ====================
    
    /**
     * 异步执行可能抛出异常的 Supplier
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param <T> 成功类型
     * @return CompletableFuture<Either>
     */
    public static <T> CompletableFuture<Either<Throwable, T>> async(Supplier<T> supplier) {
        return async(supplier, null);
    }
    
    /**
     * 异步执行可能抛出异常的 Supplier
     * 
     * @param supplier 可能抛出异常的 Supplier
     * @param executor 执行器
     * @param <T> 成功类型
     * @return CompletableFuture<Either>
     */
    public static <T> CompletableFuture<Either<Throwable, T>> async(Supplier<T> supplier, Executor executor) {
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
     * @return Either 结果列表
     */
    public static <T, R> java.util.List<Either<Throwable, R>> forEach(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return collection.stream()
                .map(t -> ofFunction(t, function))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 遍历集合，只保留成功的结果
     * 
     * @param collection 输入集合
     * @param function 转换函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 成功结果列表
     */
    public static <T, R> java.util.List<R> forEachSuccess(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return collection.stream()
                .map(t -> ofFunction(t, function))
                .filter(Either::isRight)
                .map(Either::getRight)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 遍历集合，只保留失败的结果
     * 
     * @param collection 输入集合
     * @param function 转换函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 失败结果列表
     */
    public static <T, R> java.util.List<Throwable> forEachFailure(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return collection.stream()
                .map(t -> ofFunction(t, function))
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建左值 Either
     * 
     * @param left 左值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Either
     */
    public static <L, R> Either<L, R> left(L left) {
        return Either.left(left);
    }
    
    /**
     * 创建右值 Either
     * 
     * @param right 右值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Either
     */
    public static <L, R> Either<L, R> right(R right) {
        return Either.right(right);
    }
    
    /**
     * 条件执行 Either
     * 
     * @param condition 条件
     * @param either Either 值
     * @param defaultValue 默认值
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Either
     */
    public static <L, R> Either<L, R> conditional(boolean condition, Either<L, R> either, Either<L, R> defaultValue) {
        return condition ? either : defaultValue;
    }
}
