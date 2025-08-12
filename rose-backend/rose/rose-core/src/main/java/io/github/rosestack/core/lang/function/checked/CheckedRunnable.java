package io.github.rosestack.core.lang.function.checked;

import java.util.Objects;

/**
 * 受检异常的可运行任务接口 对应 JDK 的 Runnable，但可以抛出受检异常
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

    /** 转换为 JDK Runnable（异常会被包装为 RuntimeException） */
    default Runnable unchecked() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 转换为 JDK Runnable，使用自定义异常处理器
     *
     * @param handler 异常处理器，接收捕获的异常
     * @return 标准 Runnable
     */
    default Runnable unchecked(java.util.function.Consumer<Throwable> handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        return () -> {
            try {
                run();
            } catch (Exception e) {
                handler.accept(e);
            }
        };
    }

    /** 从 JDK Runnable 创建 CheckedRunnable */
    static CheckedRunnable from(Runnable runnable) {
        Objects.requireNonNull(runnable);
        return runnable::run;
    }

    /** 组合多个 CheckedRunnable */
    default CheckedRunnable andThen(CheckedRunnable after) {
        Objects.requireNonNull(after);
        return () -> {
            run();
            after.run();
        };
    }

    /** 组合多个 CheckedRunnable */
    default CheckedRunnable andThen(Runnable after) {
        Objects.requireNonNull(after);
        return () -> {
            run();
            after.run();
        };
    }
}
