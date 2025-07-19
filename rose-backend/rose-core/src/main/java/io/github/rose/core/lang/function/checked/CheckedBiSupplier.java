package io.github.rose.core.lang.function.checked;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 受检异常的双参数供应者接口
 * 对应 JDK 的 BiFunction<T, U, R>，但可以抛出受检异常
 * 
 * @param <T> 第一个输入参数类型
 * @param <U> 返回值类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedBiSupplier<T, U> {
    
    /**
     * 获取值
     * 
     * @param t 输入参数
     * @return 供应的值
     * @throws Exception 可能抛出的异常
     */
    U get(T t) throws Exception;
    
    /**
     * 转换为 JDK BiFunction（异常会被包装为 RuntimeException）
     */
    default BiFunction<T, Void, U> unchecked() {
        return (T t, Void v) -> {
            try {
                return get(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 从 JDK BiFunction 创建 CheckedBiSupplier
     */
    static <T, U> CheckedBiSupplier<T, U> from(BiFunction<T, Void, U> function) {
        Objects.requireNonNull(function);
        return (T t) -> function.apply(t, null);
    }
    
    /**
     * 创建常量供应者
     */
    static <T, U> CheckedBiSupplier<T, U> constant(U value) {
        return (T t) -> value;
    }
}