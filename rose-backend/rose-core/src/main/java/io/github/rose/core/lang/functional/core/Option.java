package io.github.rose.core.lang.functional.core;

import io.github.rose.core.lang.functional.checked.CheckedFunction;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 简化的可选值容器
 * 提供比 JDK Optional 更丰富的 API，支持函数式编程
 * 
 * @param <T> 值的类型
 * @author rose
 */
public final class Option<T> {
    
    private static final Option<?> NONE = new Option<>(null, false);
    
    private final T value;
    private final boolean isPresent;
    
    private Option(T value, boolean isPresent) {
        this.value = value;
        this.isPresent = isPresent;
    }
    
    /**
     * 创建包含值的 Option
     */
    public static <T> Option<T> some(T value) {
        return new Option<>(Objects.requireNonNull(value), true);
    }
    
    /**
     * 创建空的 Option
     */
    @SuppressWarnings("unchecked")
    public static <T> Option<T> none() {
        return (Option<T>) NONE;
    }
    
    /**
     * 从值创建 Option（null 值会创建 none）
     */
    public static <T> Option<T> of(T value) {
        return value == null ? none() : some(value);
    }
    
    /**
     * 从 JDK Optional 创建 Option
     */
    public static <T> Option<T> from(java.util.Optional<T> optional) {
        return optional.map(Option::some).orElse(none());
    }
    
    /**
     * 检查是否包含值
     */
    public boolean isPresent() {
        return isPresent;
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return !isPresent;
    }
    
    /**
     * 获取值，如果为空则抛出异常
     */
    public T get() {
        if (isPresent) {
            return value;
        } else {
            throw new IllegalStateException("Option is empty");
        }
    }
    
    /**
     * 获取值，如果为空则返回默认值
     */
    public T getOrElse(T defaultValue) {
        return isPresent ? value : defaultValue;
    }
    
    /**
     * 获取值，如果为空则使用 Supplier 提供默认值
     */
    public T getOrElseGet(Supplier<T> supplier) {
        return isPresent ? value : supplier.get();
    }
    
    /**
     * 获取值，如果为空则抛出指定异常
     */
    public T getOrElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }
    
    /**
     * 如果包含值则执行操作
     */
    public Option<T> onPresent(Consumer<T> consumer) {
        if (isPresent) {
            consumer.accept(value);
        }
        return this;
    }
    
    /**
     * 如果为空则执行操作
     */
    public Option<T> onEmpty(Runnable runnable) {
        if (isEmpty()) {
            runnable.run();
        }
        return this;
    }
    
    /**
     * 转换值
     */
    public <R> Option<R> map(Function<T, R> mapper) {
        if (isPresent) {
            try {
                R result = mapper.apply(value);
                return result == null ? none() : some(result);
            } catch (Exception e) {
                return none();
            }
        } else {
            return none();
        }
    }
    
    /**
     * 转换值（可能抛出异常）
     */
    public <R> Option<R> mapChecked(CheckedFunction<T, R> mapper) {
        if (isPresent) {
            try {
                R result = mapper.apply(value);
                return result == null ? none() : some(result);
            } catch (Exception e) {
                return none();
            }
        } else {
            return none();
        }
    }
    
    /**
     * 扁平化转换
     */
    public <R> Option<R> flatMap(Function<T, Option<R>> mapper) {
        if (isPresent) {
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return none();
            }
        } else {
            return none();
        }
    }
    
    /**
     * 过滤值
     */
    public Option<T> filter(Predicate<T> predicate) {
        if (isPresent && predicate.test(value)) {
            return this;
        } else {
            return none();
        }
    }
    
    /**
     * 如果为空则使用默认值
     */
    public Option<T> orElse(Option<T> other) {
        return isPresent ? this : other;
    }
    
    /**
     * 如果为空则使用 Supplier 提供默认值
     */
    public Option<T> orElseGet(Supplier<Option<T>> supplier) {
        return isPresent ? this : supplier.get();
    }
    
    /**
     * 转换为 Try
     */
    public Try<T> toTry() {
        return isPresent ? Try.success(value) : Try.failure(new IllegalStateException("Option is empty"));
    }
    
    /**
     * 转换为 Either
     */
    public Either<String, T> toEither() {
        return isPresent ? Either.right(value) : Either.left("Option is empty");
    }
    
    /**
     * 转换为 JDK Optional
     */
    public java.util.Optional<T> toOptional() {
        return isPresent ? java.util.Optional.of(value) : java.util.Optional.empty();
    }
    
    /**
     * 转换为 Stream
     */
    public java.util.stream.Stream<T> stream() {
        return isPresent ? java.util.stream.Stream.of(value) : java.util.stream.Stream.empty();
    }
    
    /**
     * 匹配操作
     */
    public <R> R match(Function<T, R> presentMapper, Supplier<R> emptySupplier) {
        return isPresent ? presentMapper.apply(value) : emptySupplier.get();
    }
    
    /**
     * 执行匹配操作
     */
    public void match(Consumer<T> presentConsumer, Runnable emptyRunnable) {
        if (isPresent) {
            presentConsumer.accept(value);
        } else {
            emptyRunnable.run();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Option<?> other = (Option<?>) obj;
        if (isPresent != other.isPresent) return false;
        return isPresent ? Objects.equals(value, other.value) : true;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, isPresent);
    }
    
    @Override
    public String toString() {
        return isPresent ? "Some(" + value + ")" : "None";
    }
}