package io.github.rose.core.lang.functional.checked;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 受检异常的供应者接口
 * 对应 JDK 的 Supplier<T>，但可以抛出受检异常
 * 
 * @param <T> 供应值的类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedSupplier<T> {
    
    /**
     * 获取值
     * 
     * @return 供应的值
     * @throws Exception 可能抛出的异常
     */
    T get() throws Exception;
    
    /**
     * 转换为 JDK Supplier（异常会被包装为 RuntimeException）
     */
    default Supplier<T> unchecked() {
        return () -> {
            try {
                return get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 从 JDK Supplier 创建 CheckedSupplier
     */
    static <T> CheckedSupplier<T> from(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return supplier::get;
    }
    
    /**
     * 创建常量供应者
     */
    static <T> CheckedSupplier<T> constant(T value) {
        return () -> value;
    }
}