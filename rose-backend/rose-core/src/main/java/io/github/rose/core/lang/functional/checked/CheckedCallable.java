package io.github.rose.core.lang.functional.checked;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 受检异常的可调用任务接口
 * 对应 JDK 的 Callable<T>，但可以抛出受检异常
 * 
 * @param <T> 返回值类型
 * @author rose
 */
@FunctionalInterface
public interface CheckedCallable<T> {
    
    /**
     * 执行任务并返回结果
     * 
     * @return 任务结果
     * @throws Exception 可能抛出的异常
     */
    T call() throws Exception;
    
    /**
     * 转换为 JDK Callable（异常会被包装为 RuntimeException）
     */
    default Callable<T> unchecked() {
        return () -> {
            try {
                return call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 从 JDK Callable 创建 CheckedCallable
     */
    static <T> CheckedCallable<T> from(Callable<T> callable) {
        Objects.requireNonNull(callable);
        return callable::call;
    }
    
    /**
     * 从 CheckedSupplier 创建 CheckedCallable
     */
    static <T> CheckedCallable<T> from(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return supplier::get;
    }
    
    /**
     * 创建常量调用者
     */
    static <T> CheckedCallable<T> constant(T value) {
        return () -> value;
    }
}