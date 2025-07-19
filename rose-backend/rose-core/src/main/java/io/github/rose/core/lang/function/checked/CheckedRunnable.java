package io.github.rose.core.lang.function.checked;

import java.util.Objects;

/**
 * 受检异常的可运行任务接口
 * 对应 JDK 的 Runnable，但可以抛出受检异常
 *
 * @author rose
 */
@FunctionalInterface
public interface CheckedRunnable {

    /**
     * 执行任务
     *
     * @throws Exception 可能抛出的异常
     */
    void run() throws Exception;

    static CheckedRunnable of(CheckedRunnable methodReference) {
        return methodReference;
    }

    /**
     * 转换为 JDK Runnable（异常会被包装为 RuntimeException）
     */
    default Runnable unchecked() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                sneakyThrow(e);
            }
        };
    }

    /**
     * 从 JDK Runnable 创建 CheckedRunnable
     */
    static CheckedRunnable from(Runnable runnable) {
        Objects.requireNonNull(runnable);
        return runnable::run;
    }

    /**
     * 组合多个 CheckedRunnable
     */
    default CheckedRunnable andThen(CheckedRunnable after) {
        Objects.requireNonNull(after);
        return () -> {
            run();
            after.run();
        };
    }

    /**
     * 组合多个 CheckedRunnable
     */
    default CheckedRunnable andThen(Runnable after) {
        Objects.requireNonNull(after);
        return () -> {
            run();
            after.run();
        };
    }

    static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}