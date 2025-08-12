package io.github.rosestack.spring.boot.web.config;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步执行配置类
 *
 * <p>为 @Async 注解方法提供自定义线程池和异常处理，替代默认的 SimpleAsyncTaskExecutor。
 *
 * <p>
 *
 * <h3>核心特性：</h3>
 *
 * <ul>
 *   <li>使用自定义线程池执行异步方法
 *   <li>统一处理异步方法中的未捕获异常
 * </ul>
 *
 * <p>
 *
 * <h3>使用示例：</h3>
 *
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
 * @author chensoul
 * @see AsyncConfigurer
 * @see ThreadPoolTaskExecutor
 * @since 1.0.0
 */
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    /**
     * 系统 CPU 核心数，用于计算默认线程池大小
     */
    private final int core = Runtime.getRuntime().availableProcessors();

    private final TaskExecutionProperties taskExecutionProperties;

    /**
     * 获取异步方法执行器
     *
     * <p>返回自定义配置的线程池，替代 Spring 默认的 SimpleAsyncTaskExecutor。
     *
     * @return 配置的线程池执行器
     */
    @Override
    public Executor getAsyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 使用 Spring Boot 配置属性
        TaskExecutionProperties.Pool pool = taskExecutionProperties.getPool();
        executor.setCorePoolSize(ObjectUtils.defaultIfNull(pool.getCoreSize(), core));
        executor.setMaxPoolSize(ObjectUtils.defaultIfNull(pool.getMaxSize(), core * 2));
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setKeepAliveSeconds((int) pool.getKeepAlive().getSeconds());
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());

        // 关闭配置
        TaskExecutionProperties.Shutdown shutdown = taskExecutionProperties.getShutdown();
        executor.setWaitForTasksToCompleteOnShutdown(shutdown.isAwaitTermination());
        if (shutdown.getAwaitTerminationPeriod() != null) {
            executor.setAwaitTerminationSeconds(
                    (int) shutdown.getAwaitTerminationPeriod().getSeconds());
        }

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info(
                "创建异步线程池，核心线程数：{}，最大线程数：{}，队列容量：{}，线程前缀：{}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity(),
                executor.getThreadNamePrefix());

        return executor;
    }

    /**
     * 获取异步方法异常处理器
     *
     * <p>处理异步方法中的未捕获异常，记录异常信息并转换为 BusinessException。 包含异常消息、方法名称和参数值等上下文信息。
     *
     * @return 异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            throwable.printStackTrace();

            StringBuilder sb = new StringBuilder();
            sb.append("Exception message - ")
                    .append(throwable.getMessage())
                    .append(", Method name - ")
                    .append(method.getName());

            if (ObjectUtils.isNotEmpty(objects)) {
                sb.append(", Parameter value - ").append(Arrays.toString(objects));
            }
            throw new RuntimeException(sb.toString());
        };
    }

    /**
     * 创建定时任务线程池
     *
     * <p>创建用于执行定时任务的 ScheduledExecutorService，线程数等于 CPU 核心数。 使用守护线程，不会阻止 JVM 关闭。
     *
     * @return 定时任务执行器
     */
    @ConditionalOnMissingBean
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(
                core,
                new BasicThreadFactory.Builder()
                        .namingPattern("schedule-pool-%d")
                        .daemon(true)
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
            }
        };
    }
}
