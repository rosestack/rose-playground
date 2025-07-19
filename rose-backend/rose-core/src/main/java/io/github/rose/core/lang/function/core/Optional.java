package io.github.rose.core.lang.function.core;

import io.github.rose.core.lang.function.checked.CheckedFunction;

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
public final class Optional<T> {
    private static final Optional<?> NONE = new Optional<>(null, false);

    private final T value;
    private final boolean isPresent;

    private Optional(T value, boolean isPresent) {
        this.value = value;
        this.isPresent = isPresent;
    }

    /**
     * 创建包含值的 Optional
     */
    public static <T> Optional<T> some(T value) {
        return new Optional<>(Objects.requireNonNull(value), true);
    }

    /**
     * 创建空的 Optional
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> none() {
        return (Optional<T>) NONE;
    }

    /**
     * 从值创建 Optional（null 值会创建 none）
     */
    public static <T> Optional<T> of(T value) {
        return value == null ? none() : some(value);
    }

    /**
     * 从 JDK Optional 创建 Optional
     */
    public static <T> Optional<T> from(java.util.Optional<T> optional) {
        return optional.map(Optional::some).orElse(none());
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
            throw new IllegalStateException("Optional is empty");
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
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return isPresent ? value : supplier.get();
    }

    /**
     * 获取值，如果为空则抛出指定异常
     */
    public T getOrElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier cannot be null");
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 如果包含值则执行操作
     */
    public Optional<T> onPresent(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (isPresent) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * 如果为空则执行操作
     */
    public Optional<T> onEmpty(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (isEmpty()) {
            runnable.run();
        }
        return this;
    }

    /**
     * 转换值
     */
    public <R> Optional<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
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
     * 转换值（可能抛出异常）（重载）
     */
    public <R> Optional<R> map(CheckedFunction<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
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
    public <R> Optional<R> flatMap(Function<T, Optional<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
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
    public Optional<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        if (isPresent && predicate.test(value)) {
            return this;
        } else {
            return none();
        }
    }

    /**
     * 如果为空则使用默认值
     */
    public Optional<T> orElse(Optional<T> other) {
        Objects.requireNonNull(other, "other cannot be null");
        return isPresent ? this : other;
    }

    /**
     * 如果为空则使用 Supplier 提供默认值
     */
    public Optional<T> orElseGet(Supplier<Optional<T>> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return isPresent ? this : supplier.get();
    }

    /**
     * 转换为 Try
     */
    public Try<T> toTry() {
        return isPresent ? Try.success(value) : Try.failure(new IllegalStateException("Optional is empty"));
    }

    /**
     * 转换为 Either
     */
    public Either<String, T> toEither() {
        return isPresent ? Either.right(value) : Either.left("Optional is empty");
    }

    /**
     * 匹配操作
     */
    public <R> R match(Function<T, R> presentMapper, Supplier<R> emptySupplier) {
        Objects.requireNonNull(presentMapper, "presentMapper cannot be null");
        Objects.requireNonNull(emptySupplier, "emptySupplier cannot be null");
        return isPresent ? presentMapper.apply(value) : emptySupplier.get();
    }

    /**
     * 执行匹配操作
     */
    public void match(Consumer<T> presentConsumer, Runnable emptyRunnable) {
        Objects.requireNonNull(presentConsumer, "presentConsumer cannot be null");
        Objects.requireNonNull(emptyRunnable, "emptyRunnable cannot be null");
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
        Optional<?> other = (Optional<?>) obj;
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