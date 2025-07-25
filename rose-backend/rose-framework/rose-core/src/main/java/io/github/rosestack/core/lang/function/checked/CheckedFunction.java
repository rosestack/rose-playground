package io.github.rosestack.core.lang.function.checked;

import java.util.Objects;
import java.util.function.Function;

/**
 * 受检异常的单参数函数接口
 * 对应 JDK 的 Function<T, R>，但可以抛出受检异常
 * 
 * @param <T> 输入参数类型
 * @param <R> 返回值类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
    
    /**
     * 应用函数
     * 
     * @param t 输入参数
     * @return 函数结果
     * @throws Exception 可能抛出的异常
     */
    R apply(T t) throws Exception;
    
    /**
     * 函数组合：先应用当前函数，再应用 after 函数
     */
    default <V> CheckedFunction<T, V> andThen(CheckedFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
    
    /**
     * 与 JDK Function 组合
     */
    default <V> CheckedFunction<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
    
    /**
     * 转换为 JDK Function（异常会被包装为 RuntimeException）
     */
    default Function<T, R> unchecked() {
        return (T t) -> {
            try {
                return apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 转换为 JDK Function，使用自定义异常处理器
     * 当发生异常时，调用异常处理器并返回 null
     *
     * @param handler 异常处理器，接收捕获的异常
     * @return 标准 Function
     */
    default Function<T, R> unchecked(java.util.function.Consumer<Throwable> handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        return (T t) -> {
            try {
                return apply(t);
            } catch (Exception e) {
                handler.accept(e);
                return null;
            }
        };
    }

    /**
     * 转换为 JDK Function，使用自定义异常处理器和默认值
     * 当发生异常时，调用异常处理器并返回默认值
     *
     * @param handler 异常处理器，接收捕获的异常
     * @param defaultValue 异常时返回的默认值
     * @return 标准 Function
     */
    default Function<T, R> unchecked(java.util.function.Consumer<Throwable> handler, R defaultValue) {
        Objects.requireNonNull(handler, "handler cannot be null");
        return (T t) -> {
            try {
                return apply(t);
            } catch (Exception e) {
                handler.accept(e);
                return defaultValue;
            }
        };
    }
    
    /**
     * 从 JDK Function 创建 CheckedFunction
     */
    static <T, R> CheckedFunction<T, R> from(Function<T, R> function) {
        Objects.requireNonNull(function);
        return function::apply;
    }
    
    /**
     * 恒等函数
     */
    static <T> CheckedFunction<T, T> identity() {
        return t -> t;
    }
}