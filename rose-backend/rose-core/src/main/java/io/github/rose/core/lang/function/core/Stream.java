package io.github.rose.core.lang.function.core;

import io.github.rose.core.lang.function.checked.CheckedConsumer;
import io.github.rose.core.lang.function.checked.CheckedFunction;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 简化的流式处理容器
 * 提供比 JDK Stream 更简洁的 API，支持函数式编程
 *
 * @param <T> 流中元素的类型
 * @author rose
 */
public final class Stream<T> {

    private final java.util.stream.Stream<T> delegate;

    private Stream(java.util.stream.Stream<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * 从集合创建 Stream
     */
    public static <T> Stream<T> of(Collection<T> collection) {
        Objects.requireNonNull(collection, "collection cannot be null");
        return new Stream<>(collection.stream());
    }

    /**
     * 从数组创建 Stream
     */
    @SafeVarargs
    public static <T> Stream<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        return new Stream<>(java.util.Arrays.stream(elements));
    }

    /**
     * 从 JDK Stream 创建 Stream
     */
    public static <T> Stream<T> from(java.util.stream.Stream<T> stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return new Stream<>(stream);
    }

    /**
     * 创建空 Stream
     */
    public static <T> Stream<T> empty() {
        return new Stream<>(java.util.stream.Stream.empty());
    }

    /**
     * 创建无限 Stream
     */
    public static <T> Stream<T> generate(Supplier<T> supplier) {
        return new Stream<>(java.util.stream.Stream.generate(supplier));
    }

    /**
     * 创建范围 Stream
     */
    public static Stream<Integer> range(int start, int end) {
        return new Stream<>(java.util.stream.IntStream.range(start, end).boxed());
    }

    /**
     * 过滤元素
     */
    public Stream<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return new Stream<>(delegate.filter(predicate));
    }

    /**
     * 过滤非空元素
     */
    public Stream<T> filterNonNull() {
        return new Stream<>(delegate.filter(Objects::nonNull));
    }

    /**
     * 转换元素
     */
    public <R> Stream<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Stream<>(delegate.map(mapper));
    }

    /**
     * 转换元素（可能抛出异常）（重载）
     */
    public <R> Stream<R> map(CheckedFunction<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Stream<>(delegate.map(mapper.unchecked()));
    }

    /**
     * 扁平化转换到 Option
     */
    public <R> Stream<R> flatMapOption(Function<T, Option<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Stream<>(delegate.flatMap(t -> mapper.apply(t).stream()));
    }

    /**
     * 扁平化转换到 Try
     */
    public <R> Stream<R> flatMapTry(Function<T, Try<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Stream<>(delegate.flatMap(t -> {
            Try<R> result = mapper.apply(t);
            return result.isSuccess() ? java.util.stream.Stream.of(result.get()) : java.util.stream.Stream.empty();
        }));
    }


    /**
     * 对每个元素执行操作
     */
    public Stream<T> peek(Consumer<T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        return new Stream<>(delegate.peek(action));
    }

    /**
     * 对每个元素执行可能抛出异常的操作（重载）
     */
    public Stream<T> peek(CheckedConsumer<T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        return new Stream<>(delegate.peek(action.unchecked()));
    }

    /**
     * 查找第一个元素
     */
    public Option<T> findFirst() {
        return delegate.findFirst().map(Option::some).orElse(Option.none());
    }

    /**
     * 查找第一个匹配条件的元素
     */
    public Option<T> findFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return delegate.filter(predicate).findFirst().map(Option::some).orElse(Option.none());
    }

    /**
     * 获取最大元素
     */
    public Option<T> max(Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator cannot be null");
        return delegate.max(comparator).map(Option::some).orElse(Option.none());
    }

    /**
     * 获取最小元素
     */
    public Option<T> min(Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator cannot be null");
        return delegate.min(comparator).map(Option::some).orElse(Option.none());
    }

    /**
     * 归约操作
     */
    public Option<T> reduce(BinaryOperator<T> accumulator) {
        Objects.requireNonNull(accumulator, "accumulator cannot be null");
        return delegate.reduce(accumulator).map(Option::some).orElse(Option.none());
    }

    /**
     * 对每个元素执行操作
     */
    public void forEach(Consumer<T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        delegate.forEach(action);
    }

    /**
     * 对每个元素执行可能抛出异常的操作（重载）
     */
    public void forEach(CheckedConsumer<T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        delegate.forEach(action.unchecked());
    }
}