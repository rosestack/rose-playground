package io.github.rose.core.lang.functional.core;

import io.github.rose.core.lang.functional.checked.CheckedFunction;
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
        return new Stream<>(collection.stream());
    }
    
    /**
     * 从数组创建 Stream
     */
    @SafeVarargs
    public static <T> Stream<T> of(T... elements) {
        return new Stream<>(java.util.Arrays.stream(elements));
    }
    
    /**
     * 从 JDK Stream 创建 Stream
     */
    public static <T> Stream<T> from(java.util.stream.Stream<T> stream) {
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
        return new Stream<>(delegate.filter(predicate));
    }
    
    /**
     * 转换元素
     */
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new Stream<>(delegate.map(mapper));
    }
    
    /**
     * 转换元素（可能抛出异常）
     */
    public <R> Stream<R> mapChecked(CheckedFunction<T, R> mapper) {
        return new Stream<>(delegate.map(t -> {
            try {
                return mapper.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
    
    /**
     * 扁平化转换
     */
    public <R> Stream<R> flatMap(Function<T, java.util.stream.Stream<R>> mapper) {
        return new Stream<>(delegate.flatMap(mapper));
    }
    
    /**
     * 扁平化转换到 Option
     */
    public <R> Stream<R> flatMapOption(Function<T, Option<R>> mapper) {
        return new Stream<>(delegate.flatMap(t -> mapper.apply(t).stream()));
    }
    
    /**
     * 扁平化转换到 Try
     */
    public <R> Stream<R> flatMapTry(Function<T, Try<R>> mapper) {
        return new Stream<>(delegate.flatMap(t -> {
            Try<R> result = mapper.apply(t);
            return result.isSuccess() ? java.util.stream.Stream.of(result.get()) : java.util.stream.Stream.empty();
        }));
    }
    
    /**
     * 去重
     */
    public Stream<T> distinct() {
        return new Stream<>(delegate.distinct());
    }
    
    /**
     * 排序
     */
    public Stream<T> sorted() {
        return new Stream<>(delegate.sorted());
    }
    
    /**
     * 排序（使用比较器）
     */
    public Stream<T> sorted(Comparator<T> comparator) {
        return new Stream<>(delegate.sorted(comparator));
    }
    
    /**
     * 限制元素数量
     */
    public Stream<T> limit(long maxSize) {
        return new Stream<>(delegate.limit(maxSize));
    }
    
    /**
     * 跳过元素
     */
    public Stream<T> skip(long n) {
        return new Stream<>(delegate.skip(n));
    }
    
    /**
     * 对每个元素执行操作
     */
    public Stream<T> peek(Consumer<T> action) {
        return new Stream<>(delegate.peek(action));
    }
    
    /**
     * 收集到 List
     */
    public List<T> toList() {
        return delegate.collect(Collectors.toList());
    }
    
    /**
     * 收集到 Set
     */
    public Set<T> toSet() {
        return delegate.collect(Collectors.toSet());
    }
    
    /**
     * 收集到指定集合
     */
    public <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory) {
        return delegate.collect(Collectors.toCollection(collectionFactory));
    }
    
    /**
     * 收集到 Map
     */
    public <K, V> Map<K, V> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return delegate.collect(Collectors.toMap(keyMapper, valueMapper));
    }
    
    /**
     * 收集到 Map（处理重复键）
     */
    public <K, V> Map<K, V> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, BinaryOperator<V> mergeFunction) {
        return delegate.collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }
    
    /**
     * 收集到分组 Map
     */
    public <K> Map<K, List<T>> groupBy(Function<T, K> classifier) {
        return delegate.collect(Collectors.groupingBy(classifier));
    }
    
    /**
     * 收集到分组 Map（带下游收集器）
     */
    public <K, A, D> Map<K, D> groupBy(Function<T, K> classifier, Collector<T, A, D> downstream) {
        return delegate.collect(Collectors.groupingBy(classifier, downstream));
    }
    
    /**
     * 查找第一个元素
     */
    public Option<T> findFirst() {
        return delegate.findFirst().map(Option::some).orElse(Option.none());
    }
    
    /**
     * 查找任意元素
     */
    public Option<T> findAny() {
        return delegate.findAny().map(Option::some).orElse(Option.none());
    }
    
    /**
     * 检查是否所有元素都满足条件
     */
    public boolean allMatch(Predicate<T> predicate) {
        return delegate.allMatch(predicate);
    }
    
    /**
     * 检查是否有元素满足条件
     */
    public boolean anyMatch(Predicate<T> predicate) {
        return delegate.anyMatch(predicate);
    }
    
    /**
     * 检查是否没有元素满足条件
     */
    public boolean noneMatch(Predicate<T> predicate) {
        return delegate.noneMatch(predicate);
    }
    
    /**
     * 计算元素数量
     */
    public long count() {
        return delegate.count();
    }
    
    /**
     * 获取最大元素
     */
    public Option<T> max(Comparator<T> comparator) {
        return delegate.max(comparator).map(Option::some).orElse(Option.none());
    }
    
    /**
     * 获取最小元素
     */
    public Option<T> min(Comparator<T> comparator) {
        return delegate.min(comparator).map(Option::some).orElse(Option.none());
    }
    
    /**
     * 归约操作
     */
    public Option<T> reduce(BinaryOperator<T> accumulator) {
        return delegate.reduce(accumulator).map(Option::some).orElse(Option.none());
    }
    
    /**
     * 归约操作（带初始值）
     */
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return delegate.reduce(identity, accumulator);
    }
    
    /**
     * 归约操作（带初始值和组合器）
     */
    public <U> U reduce(U identity, BiFunction<U, T, U> accumulator, BinaryOperator<U> combiner) {
        return delegate.reduce(identity, accumulator, combiner);
    }
    
    /**
     * 收集操作
     */
    public <R, A> R collect(Collector<T, A, R> collector) {
        return delegate.collect(collector);
    }
    
    /**
     * 对每个元素执行操作
     */
    public void forEach(Consumer<T> action) {
        delegate.forEach(action);
    }
    
    /**
     * 转换为 JDK Stream
     */
    public java.util.stream.Stream<T> toJavaStream() {
        return delegate;
    }
    
    /**
     * 转换为 Try Stream
     */
    public Stream<Try<T>> toTryStream() {
        return new Stream<>(delegate.map(Try::success));
    }
    
    /**
     * 转换为 Option Stream
     */
    public Stream<Option<T>> toOptionStream() {
        return new Stream<>(delegate.map(Option::some));
    }
}