package io.github.rose.common.config;

// import io.github.rose.core.util.SpringThreadUtils;
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
 * 线程池配置
 **/
@AutoConfiguration
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class ThreadPoolConfig {
    private final int core = Runtime.getRuntime().availableProcessors();

    @Bean(name = "threadPoolTaskExecutor")
    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(TaskExecutionProperties threadPoolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ObjectUtils.defaultIfNull(threadPoolProperties.getPool().getCoreSize(), core));
        executor.setMaxPoolSize(ObjectUtils.defaultIfNull(threadPoolProperties.getPool().getCoreSize(), core * 2));
        executor.setQueueCapacity(threadPoolProperties.getPool().getQueueCapacity());
        executor.setKeepAliveSeconds((int) threadPoolProperties.getPool().getKeepAlive().toSeconds());
        executor.setAllowCoreThreadTimeOut(threadPoolProperties.getPool().isAllowCoreThreadTimeout());
        executor.setThreadNamePrefix(threadPoolProperties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(core,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                // SpringThreadUtils.printException(r, t);
            }
        };
    }
}
