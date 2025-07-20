package io.github.rose.core.lang.function.core;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 左值/右值处理容器
 * 用于处理成功/失败两种状态，比 Try 更明确地表达业务含义
 * 
 * @param <L> 左值类型（通常表示错误或异常情况）
 * @param <R> 右值类型（通常表示成功或正常情况）
 * @author rose
 */
public final class Either<L, R> {
    
    private final L left;
    private final R right;
    private final boolean isRight;
    
    private Either(L left, R right, boolean isRight) {
        this.left = left;
        this.right = right;
        this.isRight = isRight;
    }
    
    /**
     * 创建左值（错误/异常情况）
     */
    public static <L, R> Either<L, R> left(L left) {
        return new Either<>(Objects.requireNonNull(left), null, false);
    }
    
    /**
     * 创建右值（成功/正常情况）
     */
    public static <L, R> Either<L, R> right(R right) {
        return new Either<>(null, Objects.requireNonNull(right), true);
    }
    
    /**
     * 检查是否为右值（成功）
     */
    public boolean isRight() {
        return isRight;
    }
    
    /**
     * 检查是否为左值（失败）
     */
    public boolean isLeft() {
        return !isRight;
    }
    
    /**
     * 获取左值，如果为右值则抛出异常
     */
    public L getLeft() {
        if (isLeft()) {
            return left;
        } else {
            throw new IllegalStateException("Either is right, no left value available");
        }
    }
    
    /**
     * 获取右值，如果为左值则抛出异常
     */
    public R getRight() {
        if (isRight()) {
            return right;
        } else {
            throw new IllegalStateException("Either is left, no right value available");
        }
    }
    
    /**
     * 获取右值，如果为左值则返回默认值
     */
    public R getRightOrElse(R defaultValue) {
        return isRight() ? right : defaultValue;
    }
    
    /**
     * 获取右值，如果为左值则使用 Supplier 提供默认值
     */
    public R getRightOrElseGet(Supplier<R> supplier) {
        return isRight() ? right : supplier.get();
    }
    
    /**
     * 获取左值，如果为右值则返回默认值
     */
    public L getLeftOrElse(L defaultValue) {
        return isLeft() ? left : defaultValue;
    }
    
    /**
     * 获取左值，如果为右值则使用 Supplier 提供默认值
     */
    public L getLeftOrElseGet(Supplier<L> supplier) {
        return isLeft() ? left : supplier.get();
    }
    
    /**
     * 左值处理
     */
    public Either<L, R> onLeft(Consumer<L> consumer) {
        if (isLeft()) {
            consumer.accept(left);
        }
        return this;
    }
    
    /**
     * 右值处理
     */
    public Either<L, R> onRight(Consumer<R> consumer) {
        if (isRight()) {
            consumer.accept(right);
        }
        return this;
    }
    
    /**
     * 转换右值
     */
    public <T> Either<L, T> mapRight(Function<R, T> mapper) {
        if (isRight()) {
            try {
                return right(mapper.apply(right));
            } catch (Exception e) {
                return left((L) e);
            }
        } else {
            return left(left);
        }
    }
    
    /**
     * 转换左值
     */
    public <T> Either<T, R> mapLeft(Function<L, T> mapper) {
        if (isLeft()) {
            try {
                return left(mapper.apply(left));
            } catch (Exception e) {
                return left((T) e);
            }
        } else {
            return right(right);
        }
    }
    
    /**
     * 扁平化转换右值
     */
    public <T> Either<L, T> flatMapRight(Function<R, Either<L, T>> mapper) {
        if (isRight()) {
            try {
                return mapper.apply(right);
            } catch (Exception e) {
                return left((L) e);
            }
        } else {
            return left(left);
        }
    }
    
    /**
     * 扁平化转换左值
     */
    public <T> Either<T, R> flatMapLeft(Function<L, Either<T, R>> mapper) {
        if (isLeft()) {
            try {
                return mapper.apply(left);
            } catch (Exception e) {
                return left((T) e);
            }
        } else {
            return right(right);
        }
    }
    
    /**
     * 交换左右值
     */
    public Either<R, L> swap() {
        return isRight() ? left(right) : right(left);
    }
    
    /**
     * 转换为 Option
     */
    public java.util.Optional<R> toOptional() {
        return isRight() ? java.util.Optional.of(right) : java.util.Optional.empty();
    }
    
    /**
     * 转换为 Try
     */
    public Try<R> toTry() {
        return isRight() ? Try.success(right) : Try.failure((Throwable) left);
    }
    
    /**
     * 折叠操作：根据是否为右值执行不同的函数
     */
    public <T> T fold(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        return isRight() ? rightMapper.apply(right) : leftMapper.apply(left);
    }
    
    /**
     * 合并操作：将两个 Either 合并
     */
    public <T> Either<L, T> join(Either<L, T> other) {
        if (isLeft()) {
            return left(left);
        } else if (other.isLeft()) {
            return left(other.getLeft());
        } else {
            return other;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Either<?, ?> other = (Either<?, ?>) obj;
        if (isRight != other.isRight) return false;
        if (isRight) {
            return Objects.equals(right, other.right);
        } else {
            return Objects.equals(left, other.left);
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(left, right, isRight);
    }
    
    @Override
    public String toString() {
        return isRight() ? "Right(" + right + ")" : "Left(" + left + ")";
    }
}