package io.github.rose.common.config;

import io.github.rose.core.util.SpringThreadUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 线程池配置类
 * <p>
 * 提供异步任务执行和定时任务调度的线程池配置，支持通过配置文件自定义线程池参数。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>基于 CPU 核心数动态配置线程池大小</li>
 *   <li>支持配置文件自定义线程池参数</li>
 *   <li>提供异步任务和定时任务两种线程池</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * # application.yml 配置
 * thread-pool:
 *   enabled: true
 * spring:
 *   task:
 *     execution:
 *       pool:
 *         core-size: 8
 *         max-size: 16
 *         queue-capacity: 100
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see ThreadPoolTaskExecutor
 * @see ScheduledExecutorService
 */
@AutoConfiguration
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class ThreadPoolConfig {

    /** 系统 CPU 核心数，用于计算默认线程池大小 */
    private final int core = Runtime.getRuntime().availableProcessors();
    /**
     * 创建异步任务线程池
     * <p>
     * 根据配置属性创建 ThreadPoolTaskExecutor，支持自定义核心线程数、最大线程数等参数。
     * 默认使用 CPU 核心数作为基准计算线程池大小。
     *
     * @param threadPoolProperties 线程池配置属性
     * @return 配置完成的线程池执行器
     */
    @Bean(name = "threadPoolTaskExecutor")
    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(TaskExecutionProperties threadPoolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(ObjectUtils.defaultIfNull(threadPoolProperties.getPool().getCoreSize(), core));
        executor.setMaxPoolSize(ObjectUtils.defaultIfNull(threadPoolProperties.getPool().getMaxSize(), core * 2));
        executor.setQueueCapacity(threadPoolProperties.getPool().getQueueCapacity());
        executor.setKeepAliveSeconds((int) threadPoolProperties.getPool().getKeepAlive().toSeconds());
        executor.setAllowCoreThreadTimeOut(threadPoolProperties.getPool().isAllowCoreThreadTimeout());
        executor.setThreadNamePrefix(threadPoolProperties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
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
}
