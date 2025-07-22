package io.github.rose.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.*;

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
 * @see AsyncConfigurer
 * @see ThreadPoolTaskExecutor
 * @since 1.0.0
 */
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@AutoConfiguration
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    /**
     * 系统 CPU 核心数，用于计算默认线程池大小
     */
    private final int core = Runtime.getRuntime().availableProcessors();

    private final TaskExecutionProperties threadPoolProperties;

    /**
     * 获取异步方法执行器
     * <p>
     * 返回自定义配置的线程池，替代 Spring 默认的 SimpleAsyncTaskExecutor。
     *
     * @return 配置的线程池执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ObjectUtils.defaultIfNull(threadPoolProperties.getPool().getCoreSize(), core));
        executor.setMaxPoolSize(ObjectUtils.defaultIfNull(threadPoolProperties.getPool().getMaxSize(), core * 2));
        executor.setQueueCapacity(threadPoolProperties.getPool().getQueueCapacity());
        executor.setKeepAliveSeconds((int) threadPoolProperties.getPool().getKeepAlive().toSeconds());
        executor.setAllowCoreThreadTimeOut(threadPoolProperties.getPool().isAllowCoreThreadTimeout());
        executor.setThreadNamePrefix(threadPoolProperties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    /**
     * 创建定时任务线程池
     * <p>
     * 创建用于执行定时任务的 ScheduledExecutorService，线程数等于 CPU 核心数。
     * 使用守护线程，不会阻止 JVM 关闭。
     *
     * @return 定时任务执行器
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(core,
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
            throw new RuntimeException(sb.toString());
        };
    }

    public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor, InitializingBean, DisposableBean {
        static final String EXCEPTION_MESSAGE = "Caught async exception";
        private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);
        private final AsyncTaskExecutor executor;

        public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void execute(Runnable task) {
            executor.execute(createWrappedRunnable(task));
        }

        @Override
        public Future<?> submit(Runnable task) {
            return executor.submit(createWrappedRunnable(task));
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return executor.submit(createCallable(task));
        }

        @Override
        public void destroy() throws Exception {
            if (executor instanceof DisposableBean) {
                DisposableBean bean = (DisposableBean) executor;
                bean.destroy();
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            if (executor instanceof InitializingBean) {
                InitializingBean bean = (InitializingBean) executor;
                bean.afterPropertiesSet();
            }
        }

        private <T> Callable<T> createCallable(Callable<T> task) {
            return () -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    handle(e);
                    throw e;
                }
            };
        }

        private Runnable createWrappedRunnable(Runnable task) {
            return () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    handle(e);
                }
            };
        }

        protected void handle(Exception e) {
            log.error(EXCEPTION_MESSAGE, e);
        }
    }
}
