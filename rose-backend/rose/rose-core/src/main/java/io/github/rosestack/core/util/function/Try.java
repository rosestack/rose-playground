package io.github.rosestack.core.util.function;

import io.github.rosestack.core.util.function.checked.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 重构后的异常处理容器
 * 提供比 Vavr Try 更简单、更易用的 API
 *
 * 重构说明：
 * - 保留核心的 Try 语义和实例方法
 * - 保留核心工厂方法和 of* 系列方法
 * - 移除 try* 系列工具方法到 Trys 类
 * - 移除 throw* 系列方法到 Preconditions 类
 *
 * @param <T> 成功时的值类型
 * @author rose
 */
@Slf4j
public final class Try<T> {
    private final T value;
    private final Throwable cause;
    private final boolean isSuccess;

    private Try(T value, Throwable cause, boolean isSuccess) {
        this.value = value;
        this.cause = cause;
        this.isSuccess = isSuccess;
    }

    // ==================== 核心状态检查方法 ====================

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

    // ==================== 值获取方法 ====================

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
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return isSuccess ? value : supplier.get();
    }

    /**
     * 获取成功值，失败时抛出指定异常
     */
    public T getOrElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier cannot be null");
        if (isSuccess) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
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

    // ==================== 副作用方法 ====================

    /**
     * 成功时执行操作
     */
    public Try<T> onSuccess(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (isSuccess) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * 失败时执行操作
     */
    public Try<T> onFailure(Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (isFailure()) {
            consumer.accept(cause);
        }
        return this;
    }

    // ==================== 函数式转换方法 ====================

    /**
     * 转换成功值（可能抛出异常）
     */
    public <R> Try<R> map(CheckedFunction<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
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
     * 安全转换（不会抛出异常）
     */
    public <R> Try<R> mapSafe(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
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
        Objects.requireNonNull(mapper, "mapper cannot be null");
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
     * 过滤值
     */
    public Try<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        if (isSuccess) {
            try {
                if (predicate.test(value)) {
                    return this;
                } else {
                    return failure(new IllegalArgumentException("Value does not match predicate"));
                }
            } catch (Throwable e) {
                return failure(e);
            }
        } else {
            return this;
        }
    }

    /**
     * 恢复失败（可能抛出异常）
     */
    public Try<T> recover(CheckedFunction<Throwable, T> recovery) {
        Objects.requireNonNull(recovery, "recovery cannot be null");
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
     * 安全恢复（不会抛出异常）
     */
    public Try<T> recoverSafe(Function<Throwable, T> recovery) {
        Objects.requireNonNull(recovery, "recovery cannot be null");
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
     * 恢复为另一个 Try
     */
    public Try<T> recoverWith(Function<Throwable, Try<T>> recovery) {
        Objects.requireNonNull(recovery, "recovery cannot be null");
        if (isSuccess) {
            return this;
        } else {
            try {
                return recovery.apply(cause);
            } catch (Throwable e) {
                return failure(e);
            }
        }
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为 JDK Optional
     */
    public Optional<T> toOptional() {
        return isSuccess ? Optional.ofNullable(value) : Optional.empty();
    }

    /**
     * 转换为自定义 Option
     */
    public Option<T> toOption() {
        return isSuccess ? Option.of(value) : Option.none();
    }

    // ==================== Object 方法重写 ====================

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

    // ==================== 静态工厂方法 ====================

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
        Objects.requireNonNull(error, "error cannot be null");
        return new Try<>(null, error, false);
    }

    /**
     * 从值创建 Try（null 值会创建成功的 Try）
     */
    public static <T> Try<T> of(T value) {
        return success(value);
    }

    /**
     * 从 JDK Optional 创建 Try
     */
    public static <T> Try<T> fromOptional(Optional<T> optional) {
        Objects.requireNonNull(optional, "optional cannot be null");
        return optional.map(Try::success).orElse(failure(new IllegalArgumentException("Optional is empty")));
    }

    /**
     * 从自定义 Option 创建 Try
     */
    public static <T> Try<T> fromOption(Option<T> option) {
        Objects.requireNonNull(option, "option cannot be null");
        return option.isPresent() ? success(option.get()) : failure(new IllegalArgumentException("Option is empty"));
    }

    // ==================== of* 系列工厂方法 ====================

    /**
     * 从 Supplier 创建 Try
     */
    public static <T> Try<T> ofSupplier(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedSupplier 创建 Try
     */
    public static <T> Try<T> ofCheckedSupplier(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 Function 创建 Try
     */
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
    public static <T, R> Try<R> ofCheckedFunction(T input, CheckedFunction<T, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return success(function.apply(input));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 BiFunction 创建 Try
     */
    public static <T, U, R> Try<R> ofBiFunction(T t, U u, BiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return success(function.apply(t, u));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedBiFunction 创建 Try
     */
    public static <T, U, R> Try<R> ofCheckedBiFunction(T t, U u, CheckedBiFunction<T, U, R> function) {
        Objects.requireNonNull(function, "function cannot be null");
        try {
            return success(function.apply(t, u));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 Consumer 创建 Try<Void>
     */
    public static <T> Try<Void> ofConsumer(T input, Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(input);
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedConsumer 创建 Try<Void>
     */
    public static <T> Try<Void> ofCheckedConsumer(T input, CheckedConsumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(input);
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 BiConsumer 创建 Try<Void>
     */
    public static <T, U> Try<Void> ofBiConsumer(T t, U u, BiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(t, u);
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedBiConsumer 创建 Try<Void>
     */
    public static <T, U> Try<Void> ofCheckedBiConsumer(T t, U u, CheckedBiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        try {
            consumer.accept(t, u);
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 Runnable 创建 Try<Void>
     */
    public static Try<Void> ofRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        try {
            runnable.run();
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedRunnable 创建 Try<Void>
     */
    public static Try<Void> ofCheckedRunnable(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        try {
            runnable.run();
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 Callable 创建 Try
     */
    public static <T> Try<T> ofCallable(Callable<T> callable) {
        Objects.requireNonNull(callable, "callable cannot be null");
        try {
            return success(callable.call());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 Predicate 创建 Try<Boolean>
     */
    public static <T> Try<Boolean> ofPredicate(T input, Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        try {
            return success(predicate.test(input));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedPredicate 创建 Try<Boolean>
     */
    public static <T> Try<Boolean> ofCheckedPredicate(T input, CheckedPredicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        try {
            return success(predicate.test(input));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 BiPredicate 创建 Try<Boolean>
     */
    public static <T, U> Try<Boolean> ofBiPredicate(T t, U u, BiPredicate<T, U> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        try {
            return success(predicate.test(t, u));
        } catch (Throwable e) {
            return failure(e);
        }
    }

    /**
     * 从 CheckedBiPredicate 创建 Try<Boolean>
     */
    public static <T, U> Try<Boolean> ofCheckedBiPredicate(T t, U u, CheckedBiPredicate<T, U> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        try {
            return success(predicate.test(t, u));
        } catch (Throwable e) {
            return failure(e);
        }
    }
}
