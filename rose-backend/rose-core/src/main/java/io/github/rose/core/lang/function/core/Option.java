package io.github.rose.core.lang.function.core;

import io.github.rose.core.lang.function.checked.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(Option.class);

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

    public static <T, R> Option<R> of(T input, CheckedFunction<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            R result = function.apply(input);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            return Option.none();
        }
    }

    public static <T, R> Option<R> of(CheckedFunction<T, R> function, T input,
                                            java.util.function.Function<Exception, R> exceptionHandler) {
        Objects.requireNonNull(function, "function cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        try {
            R result = function.apply(input);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            R fallback = exceptionHandler.apply(e);
            return fallback != null ? Option.some(fallback) : Option.none();
        }
    }

    public static <T, U> Option<Void> of(T t, U u, CheckedBiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(t, u);
            return Option.some(null);
        } catch (Exception e) {
            return Option.none();
        }
    }

    public static <T, U, R> Option<R> of(T t, U u, CheckedBiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            R result = function.apply(t, u);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            return Option.none();
        }
    }

    public static <T, U> Option<U> of(T t, CheckedBiSupplier<T, U> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            U result = supplier.get(t);
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            log.error("BiSupplier threw exception: {}", e.getMessage());
            return Option.none();
        }
    }

    public static <T> Option<T> of(CheckedCallable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        try {
            T result = callable.call();
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            return Option.none();
        }
    }

    public static <T> Option<T> of(CheckedCallable<T> callable,
                                   Function<Exception, T> exceptionHandler) {
        Objects.requireNonNull(callable, "callable cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        try {
            T result = callable.call();
            return result != null ? Option.some(result) : Option.none();
        } catch (Exception e) {
            T fallback = exceptionHandler.apply(e);
            return fallback != null ? Option.some(fallback) : Option.none();
        }
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