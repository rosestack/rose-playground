package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.core.Stream;
import io.github.rose.core.lang.function.core.Try;
import io.github.rose.core.lang.function.core.Option;
import io.github.rose.core.lang.function.core.Either;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Stream 工具类
 * 提供基于 Stream 的便捷方法
 * 
 * @author rose
 */
public final class StreamUtils {
    
    private StreamUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 创建方法 ====================
    
    /**
     * 从集合创建 Stream
     * 
     * @param collection 集合
     * @param <T> 元素类型
     * @return Stream
     */
    public static <T> Stream<T> of(java.util.Collection<T> collection) {
        Objects.requireNonNull(collection, "collection cannot be null");
        return Stream.of(collection);
    }
    
    /**
     * 从数组创建 Stream
     * 
     * @param elements 元素数组
     * @param <T> 元素类型
     * @return Stream
     */
    @SafeVarargs
    public static <T> Stream<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        return Stream.of(elements);
    }
    
    /**
     * 从 JDK Stream 创建 Stream
     * 
     * @param stream JDK Stream
     * @param <T> 元素类型
     * @return Stream
     */
    public static <T> Stream<T> from(java.util.stream.Stream<T> stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return Stream.from(stream);
    }
    
    /**
     * 创建空 Stream
     * 
     * @param <T> 元素类型
     * @return 空 Stream
     */
    public static <T> Stream<T> empty() {
        return Stream.empty();
    }
    
    /**
     * 创建单元素 Stream
     * 
     * @param element 单个元素
     * @param <T> 元素类型
     * @return Stream
     */
    public static <T> Stream<T> single(T element) {
        return Stream.of(element);
    }
    
    /**
     * 从 Option 创建 Stream
     * 
     * @param option Option
     * @param <T> 元素类型
     * @return Stream
     */
    public static <T> Stream<T> fromOption(Option<T> option) {
        Objects.requireNonNull(option, "option cannot be null");
        return option.isPresent() ? Stream.of(option.get()) : Stream.empty();
    }
    
    /**
     * 从 Try 创建 Stream
     * 
     * @param tryValue Try
     * @param <T> 元素类型
     * @return Stream
     */
    public static <T> Stream<T> fromTry(Try<T> tryValue) {
        Objects.requireNonNull(tryValue, "tryValue cannot be null");
        return tryValue.isSuccess() ? Stream.of(tryValue.get()) : Stream.empty();
    }
    
    /**
     * 从 Either 创建 Stream
     * 
     * @param either Either
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return Stream
     */
    public static <L, R> Stream<R> fromEither(Either<L, R> either) {
        Objects.requireNonNull(either, "either cannot be null");
        return either.isRight() ? Stream.of(either.getRight()) : Stream.empty();
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 将 Stream 转换为 List
     * 
     * @param stream Stream
     * @param <T> 元素类型
     * @return List
     */
    public static <T> java.util.List<T> toList(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return stream.toList();
    }
    
    /**
     * 将 Stream 转换为 Set
     * 
     * @param stream Stream
     * @param <T> 元素类型
     * @return Set
     */
    public static <T> java.util.Set<T> toSet(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return stream.toSet();
    }
    
    /**
     * 将 Stream 转换为数组
     *
     * @param stream Stream
     * @param generator 数组生成器
     * @param <T> 元素类型
     * @return 数组
     */
    public static <T> T[] toArray(Stream<T> stream, java.util.function.IntFunction<T[]> generator) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(generator, "generator cannot be null");
        return stream.toJavaStream().toArray(generator);
    }
    
    // ==================== 过滤和映射方法 ====================
    
    /**
     * 过滤非空元素
     * 
     * @param stream Stream
     * @param <T> 元素类型
     * @return 过滤后的 Stream
     */
    public static <T> Stream<T> filterNonNull(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return stream.filter(Objects::nonNull);
    }
    
    /**
     * 映射并过滤非空结果
     * 
     * @param stream Stream
     * @param mapper 映射函数
     * @param <T> 输入元素类型
     * @param <R> 输出元素类型
     * @return 映射并过滤后的 Stream
     */
    public static <T, R> Stream<R> mapNonNull(Stream<T> stream, Function<T, R> mapper) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return stream.map(mapper).filter(Objects::nonNull);
    }
    
    /**
     * 映射为 Option 并过滤有值的结果
     * 
     * @param stream Stream
     * @param mapper 映射函数
     * @param <T> 输入元素类型
     * @param <R> 输出元素类型
     * @return 映射并过滤后的 Stream
     */
    public static <T, R> Stream<R> mapOption(Stream<T> stream, Function<T, Option<R>> mapper) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return stream.map(mapper).filter(Option::isPresent).map(Option::get);
    }
    
    /**
     * 映射为 Try 并过滤成功的结果
     * 
     * @param stream Stream
     * @param mapper 映射函数
     * @param <T> 输入元素类型
     * @param <R> 输出元素类型
     * @return 映射并过滤后的 Stream
     */
    public static <T, R> Stream<R> mapTry(Stream<T> stream, Function<T, Try<R>> mapper) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return stream.map(mapper).filter(Try::isSuccess).map(Try::get);
    }
    
    /**
     * 映射为 Either 并过滤右值结果
     * 
     * @param stream Stream
     * @param mapper 映射函数
     * @param <T> 输入元素类型
     * @param <L> 左值类型
     * @param <R> 右值类型
     * @return 映射并过滤后的 Stream
     */
    public static <T, L, R> Stream<R> mapEither(Stream<T> stream, Function<T, Either<L, R>> mapper) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return stream.map(mapper).filter(Either::isRight).map(Either::getRight);
    }
    
    // ==================== 批量处理方法 ====================
    
    /**
     * 批量处理集合，返回 Try 结果流
     * 
     * @param collection 输入集合
     * @param function 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return Try 结果流
     */
    public static <T, R> Stream<Try<R>> forEachTry(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return Stream.of(collection).map(t -> Try.of(() -> function.apply(t)));
    }
    
    /**
     * 批量处理集合，返回 Option 结果流
     * 
     * @param collection 输入集合
     * @param function 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return Option 结果流
     */
    public static <T, R> Stream<Option<R>> forEachOption(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return Stream.of(collection).map(t -> {
            try {
                R result = function.apply(t);
                return result != null ? Option.some(result) : Option.none();
            } catch (Exception e) {
                return Option.none();
            }
        });
    }
    
    /**
     * 批量处理集合，返回 Either 结果流
     * 
     * @param collection 输入集合
     * @param function 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return Either 结果流
     */
    public static <T, R> Stream<Either<Throwable, R>> forEachEither(java.util.Collection<T> collection, Function<T, R> function) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        return Stream.of(collection).map(t -> {
            try {
                return Either.right(function.apply(t));
            } catch (Exception e) {
                return Either.left(e);
            }
        });
    }
    
    // ==================== 异步处理方法 ====================
    
    /**
     * 异步处理集合
     * 
     * @param collection 输入集合
     * @param function 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return CompletableFuture<List<R>>
     */
    public static <T, R> CompletableFuture<java.util.List<R>> asyncForEach(java.util.Collection<T> collection, Function<T, R> function) {
        return asyncForEach(collection, function, null);
    }
    
    /**
     * 异步处理集合
     * 
     * @param collection 输入集合
     * @param function 处理函数
     * @param executor 执行器
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return CompletableFuture<List<R>>
     */
    public static <T, R> CompletableFuture<java.util.List<R>> asyncForEach(java.util.Collection<T> collection, Function<T, R> function, Executor executor) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(function, "function cannot be null");
        
        java.util.List<CompletableFuture<R>> futures = collection.stream()
                .map(t -> {
                    if (executor != null) {
                        return CompletableFuture.supplyAsync(() -> function.apply(t), executor);
                    } else {
                        return CompletableFuture.supplyAsync(() -> function.apply(t));
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(java.util.stream.Collectors.toList()));
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 连接多个 Stream
     * 
     * @param streams Stream 数组
     * @param <T> 元素类型
     * @return 连接后的 Stream
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        Objects.requireNonNull(streams, "streams cannot be null");
        
        java.util.stream.Stream<T> result = java.util.stream.Stream.empty();
        for (Stream<T> stream : streams) {
            if (stream != null) {
                result = java.util.stream.Stream.concat(result, stream.toJavaStream());
            }
        }
        return Stream.from(result);
    }
    
    /**
     * 生成无限 Stream
     * 
     * @param supplier 元素供应者
     * @param <T> 元素类型
     * @return 无限 Stream
     */
    public static <T> Stream<T> generate(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return Stream.from(java.util.stream.Stream.generate(supplier));
    }
    
    /**
     * 生成有限 Stream
     * 
     * @param supplier 元素供应者
     * @param limit 限制数量
     * @param <T> 元素类型
     * @return 有限 Stream
     */
    public static <T> Stream<T> generate(Supplier<T> supplier, long limit) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be non-negative");
        }
        return Stream.from(java.util.stream.Stream.generate(supplier).limit(limit));
    }
    
    /**
     * 创建范围 Stream
     * 
     * @param start 开始值（包含）
     * @param end 结束值（不包含）
     * @return 范围 Stream
     */
    public static Stream<Integer> range(int start, int end) {
        return Stream.from(java.util.stream.IntStream.range(start, end).boxed());
    }
    
    /**
     * 创建闭区间范围 Stream
     * 
     * @param start 开始值（包含）
     * @param end 结束值（包含）
     * @return 范围 Stream
     */
    public static Stream<Integer> rangeClosed(int start, int end) {
        return Stream.from(java.util.stream.IntStream.rangeClosed(start, end).boxed());
    }
}
