package io.github.rose.core.lang.functional.core;

import io.github.rose.core.lang.functional.checked.CheckedFunction;
import io.github.rose.core.lang.functional.checked.CheckedSupplier;
import java.util.Objects;
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
    private final Throwable error;
    private final boolean isSuccess;
    
    private Try(T value, Throwable error, boolean isSuccess) {
        this.value = value;
        this.error = error;
        this.isSuccess = isSuccess;
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
    public static <T> Try<T> ofChecked(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e);
        }
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
            throw new RuntimeException(error);
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
    public Throwable getError() {
        if (isSuccess) {
            throw new IllegalStateException("Try is successful, no error available");
        }
        return error;
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
            consumer.accept(error);
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
            return failure(error);
        }
    }
    
    /**
     * 转换成功值（可能抛出异常）
     */
    public <R> Try<R> mapChecked(CheckedFunction<T, R> mapper) {
        if (isSuccess) {
            try {
                return success(mapper.apply(value));
            } catch (Throwable e) {
                return failure(e);
            }
        } else {
            return failure(error);
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
            return failure(error);
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
                return success(recovery.apply(error));
            } catch (Throwable e) {
                return failure(e);
            }
        }
    }
    
    /**
     * 恢复失败（可能抛出异常）
     */
    public Try<T> recoverChecked(CheckedFunction<Throwable, T> recovery) {
        if (isSuccess) {
            return this;
        } else {
            try {
                return success(recovery.apply(error));
            } catch (Throwable e) {
                return failure(e);
            }
        }
    }
    
    /**
     * 扁平化恢复失败
     */
    public Try<T> recoverWith(Function<Throwable, Try<T>> recovery) {
        if (isSuccess) {
            return this;
        } else {
            try {
                return recovery.apply(error);
            } catch (Throwable e) {
                return failure(e);
            }
        }
    }
    
    /**
     * 转换为 Optional
     */
    public java.util.Optional<T> toOptional() {
        return isSuccess ? java.util.Optional.of(value) : java.util.Optional.empty();
    }
    
    /**
     * 转换为 Either
     */
    public Either<Throwable, T> toEither() {
        return isSuccess ? Either.right(value) : Either.left(error);
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
            return Objects.equals(error, other.error);
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, error, isSuccess);
    }
    
    @Override
    public String toString() {
        return isSuccess ? "Success(" + value + ")" : "Failure(" + error + ")";
    }
}