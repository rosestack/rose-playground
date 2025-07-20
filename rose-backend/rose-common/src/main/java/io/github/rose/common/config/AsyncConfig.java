package io.github.rose.common.config;

import io.github.rose.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * 异步执行配置类
 * <p>
 * 为 @Async 注解方法提供自定义线程池和异常处理，替代默认的 SimpleAsyncTaskExecutor。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>使用自定义线程池执行异步方法</li>
 *   <li>统一处理异步方法中的未捕获异常</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Service
 * public class UserService {
 *
 *     @Async
 *     public CompletableFuture<String> processUserAsync(Long userId) {
 *         // 异步处理用户数据
 *         return CompletableFuture.completedFuture("处理完成");
 *     }
 * }
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see AsyncConfigurer
 * @see ThreadPoolTaskExecutor
 */
@EnableAsync(proxyTargetClass = true)
@AutoConfiguration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    /** 异步方法执行的线程池，由 ThreadPoolConfig 提供 */
    @Qualifier("threadPoolTaskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 获取异步方法执行器
     * <p>
     * 返回自定义配置的线程池，替代 Spring 默认的 SimpleAsyncTaskExecutor。
     *
     * @return 配置的线程池执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return threadPoolTaskExecutor;
    }

    /**
     * 获取异步方法异常处理器
     * <p>
     * 处理异步方法中的未捕获异常，记录异常信息并转换为 BusinessException。
     * 包含异常消息、方法名称和参数值等上下文信息。
     *
     * @return 异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            throwable.printStackTrace();

            StringBuilder sb = new StringBuilder();
            sb.append("Exception message - ").append(throwable.getMessage())
                    .append(", Method name - ").append(method.getName());

            if (ObjectUtils.isNotEmpty(objects)) {
                sb.append(", Parameter value - ").append(Arrays.toString(objects));
            }
            throw new BusinessException(sb.toString());
        };
    }
}
