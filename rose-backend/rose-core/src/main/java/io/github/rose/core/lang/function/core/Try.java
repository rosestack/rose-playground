package io.github.rose.core.lang.function.core;

import io.github.rose.core.lang.function.checked.*;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 简化的异常处理容器
 * 提供比 Vavr Try 更简单、更易用的 API
 *
 * @param <T> 成功时的值类型
 * @author rose
 */
public final class Try<T> {

    private final T value;
    private final Throwable cause;
    private final boolean isSuccess;

    private Try(T value, Throwable cause, boolean isSuccess) {
        this.value = value;
        this.cause = cause;
        this.isSuccess = isSuccess;
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * 检查是否失败
     */
    public boolean isFailure() {
        return !isSuccess;
    }

    /**
     * 获取成功值，失败时抛出异常
     */
    public T get() {
        if (isSuccess) {
            return value;
        } else {
            throw new RuntimeException(cause);
        }
    }

    /**
     * 获取成功值，失败时返回默认值
     */
    public T getOrElse(T defaultValue) {
        return isSuccess ? value : defaultValue;
    }

    /**
     * 获取成功值，失败时使用 Supplier 提供默认值
     */
    public T getOrElseGet(Supplier<T> supplier) {
        return isSuccess ? value : supplier.get();
    }

    /**
     * 获取错误信息
     */
    public Throwable getCause() {
        if (isSuccess) {
            throw new IllegalStateException("Try is successful, no error available");
        }
        return cause;
    }

    /**
     * 成功时执行操作
     */
    public Try<T> onSuccess(Consumer<T> consumer) {
        if (isSuccess) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * 失败时执行操作
     */
    public Try<T> onFailure(Consumer<Throwable> consumer) {
        if (isFailure()) {
            consumer.accept(cause);
        }
        return this;
    }

    /**
     * 转换成功值
     */
    public <R> Try<R> map(Function<T, R> mapper) {
        if (isSuccess) {
            try {
                return success(mapper.apply(value));
            } catch (Throwable e) {
                return failure(e);
            }
        } else {
            return failure(cause);
        }
    }

    /**
     * 转换成功值（可能抛出异常）
     */
    public <R> Try<R> map(CheckedFunction<T, R> mapper) {
        if (isSuccess) {
            try {
                return success(mapper.apply(value));
            } catch (Throwable e) {
                return failure(e);
            }
        } else {
            return failure(cause);
        }
    }

    /**
     * 扁平化转换
     */
    public <R> Try<R> flatMap(Function<T, Try<R>> mapper) {
        if (isSuccess) {
            try {
                return mapper.apply(value);
            } catch (Throwable e) {
                return failure(e);
            }
        } else {
            return failure(cause);
        }
    }

    /**
     * 恢复失败
     */
    public Try<T> recover(Function<Throwable, T> recovery) {
        if (isSuccess) {
            return this;
        } else {
            try {
                return success(recovery.apply(cause));
            } catch (Throwable e) {
                return failure(e);
            }
        }
    }

    /**
     * 恢复失败（可能抛出异常）
     */
    public Try<T> recover(CheckedFunction<Throwable, T> recovery) {
        if (isSuccess) {
            return this;
        } else {
            try {
                return success(recovery.apply(cause));
            } catch (Throwable e) {
                return failure(e);
            }
        }
    }

    /**
     * 转换为 Option
     */
    public java.util.Optional<T> toOptional() {
        return isSuccess ? java.util.Optional.of(value) : java.util.Optional.empty();
    }

    /**
     * 创建成功的 Try
     */
    public static <T> Try<T> success(T value) {
        return new Try<>(value, null, true);
    }

    /**
     * 创建失败的 Try
     */
    public static <T> Try<T> failure(Throwable error) {
        return new Try<>(null, Objects.requireNonNull(error), false);
    }

    /**
     * 从可能抛出异常的 Supplier 创建 Try
     */
    public static <T> Try<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从可能抛出异常的函数创建 Try
     */
    public static <T> Try<T> ofSupplier(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    public static <T, R> Try<R> ofFunction(T input, Function<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return success(function.apply(input));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedFunction 创建 Try
     */
    public static <T, R> Try<R> ofFunction(T input, CheckedFunction<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return success(function.apply(input));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedBiFunction 创建 Try
     */
    public static <T, U, R> Try<R> ofFunction(T t, U u, CheckedBiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return success(function.apply(t, u));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedConsumer 创建 Try<Void>
     */
    public static <T> Try<Void> ofConsumer(T input, CheckedConsumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(input);
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedBiConsumer 创建 Try<Void>
     */
    public static <T, U> Try<Void> ofConsumer(T t, U u, CheckedBiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(t, u);
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }


    /**
     * 从 CheckedRunnable 创建 Try<Void>
     */
    public static Try<Void> ofRunnable(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        try {
            runnable.run();
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    static Try<Void> ofRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        return ofRunnable((CheckedRunnable) runnable::run);
    }

    public static <T> Try<T> ofCallable(Callable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        return ofSupplier(callable::call);
    }

    /**
     * 从 CheckedPredicate 创建 Try
     */
    public static <T> Try<Boolean> ofPredicate(T input, CheckedPredicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        try {
            return success(predicate.test(input));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedBiPredicate 创建 Try
     */
    public static <T, U> Try<Boolean> ofPredicate(T t, U u, CheckedBiPredicate<T, U> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        try {
            return success(predicate.test(t, u));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 转换为 Either
     */
    public Either<Throwable, T> toEither() {
        return isSuccess ? Either.right(value) : Either.left(cause);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Try<?> other = (Try<?>) obj;
        if (isSuccess != other.isSuccess) return false;
        if (isSuccess) {
            return Objects.equals(value, other.value);
        } else {
            return Objects.equals(cause, other.cause);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, cause, isSuccess);
    }

    @Override
    public String toString() {
        return isSuccess ? "Success(" + value + ")" : "Failure(" + cause + ")";
    }
}