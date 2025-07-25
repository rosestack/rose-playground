package io.github.rose.core.lang.function;

import io.github.rose.core.lang.function.checked.CheckedConsumer;
import io.github.rose.core.lang.function.checked.CheckedFunction;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 简化的流式处理容器
 * 提供比 JDK Streams 更简洁的 API，支持函数式编程
 *
 * @param <T> 流中元素的类型
 * @author rose
 */
public final class Streams<T> {

    private final Stream<T> delegate;

    private Streams(Stream<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * 从集合创建 Streams
     */
    public static <T> Streams<T> of(Collection<T> collection) {
        Objects.requireNonNull(collection, "collection cannot be null");
        return new Streams<>(collection.stream());
    }

    /**
     * 从数组创建 Streams
     */
    @SafeVarargs
    public static <T> Streams<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        return new Streams<>(java.util.Arrays.stream(elements));
    }

    /**
     * 从 JDK Streams 创建 Streams
     */
    public static <T> Streams<T> from(Stream<T> stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return new Streams<>(stream);
    }

    /**
     * 创建空 Streams
     */
    public static <T> Streams<T> empty() {
        return new Streams<>(Stream.empty());
    }

    /**
     * 创建无限 Streams
     */
    public static <T> Streams<T> generate(Supplier<T> supplier) {
        return new Streams<>(Stream.generate(supplier));
    }

    /**
     * 创建范围 Streams
     */
    public static Streams<Integer> range(int start, int end) {
        return new Streams<>(IntStream.range(start, end).boxed());
    }

    /**
     * 过滤元素
     */
    public Streams<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return new Streams<>(delegate.filter(predicate));
    }

    /**
     * 过滤非空元素
     */
    public Streams<T> filterNonNull() {
        return new Streams<>(delegate.filter(Objects::nonNull));
    }

    /**
     * 转换元素
     */
    public <R> Streams<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Streams<>(delegate.map(mapper));
    }

    /**
     * 转换元素（可能抛出异常）（重载）
     */
    public <R> Streams<R> map(CheckedFunction<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Streams<>(delegate.map(mapper.unchecked()));
    }

    /**
     * 扁平化转换到 Option
     */
    public <R> Streams<R> flatMapOption(Function<T, Option<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Streams<>(delegate.flatMap(t -> {
            Option<R> option = mapper.apply(t);
            return option.isPresent() ? Stream.of(option.get()) : Stream.empty();
        }));
    }

    /**
     * 扁平化转换到 Try
     */
    public <R> Streams<R> flatMapTry(Function<T, Try<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new Streams<>(delegate.flatMap(t -> {
            Try<R> result = mapper.apply(t);
            return result.isSuccess() ? Stream.of(result.get()) : Stream.empty();
        }));
    }

    /**
     * 对每个元素执行操作
     */
    public Streams<T> peek(Consumer<T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        return new Streams<>(delegate.peek(action));
    }

    /**
     * 对每个元素执行可能抛出异常的操作（重载）
     */
    public Streams<T> peek(CheckedConsumer<T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        return new Streams<>(delegate.peek(action.unchecked()));
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