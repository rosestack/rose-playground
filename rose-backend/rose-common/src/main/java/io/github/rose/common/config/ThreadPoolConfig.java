package io.github.rose.common.config;

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
 * Thread pool configuration for asynchronous and scheduled task execution.
 *
 * This configuration class provides centralized thread pool management for the application,
 * offering both general-purpose asynchronous execution and scheduled task execution capabilities.
 * It integrates with Spring Boot's TaskExecutionProperties for flexible configuration and
 * provides sensible defaults based on system capabilities.
 *
 * <h3>Thread Pool Types:</h3>
 * <ul>
 *   <li><strong>ThreadPoolTaskExecutor:</strong> For general asynchronous task execution (@Async methods)</li>
 *   <li><strong>ScheduledExecutorService:</strong> For periodic and delayed task execution (@Scheduled methods)</li>
 * </ul>
 *
 * <h3>Configuration Features:</h3>
 * <ul>
 *   <li><strong>Dynamic Sizing:</strong> Thread pool sizes based on available CPU cores</li>
 *   <li><strong>Property Integration:</strong> Configurable through Spring Boot properties</li>
 *   <li><strong>Conditional Creation:</strong> Thread pools created only when enabled</li>
 *   <li><strong>Rejection Handling:</strong> CallerRunsPolicy for graceful degradation</li>
 *   <li><strong>Thread Naming:</strong> Meaningful thread names for debugging</li>
 * </ul>
 *
 * <h3>Default Sizing Strategy:</h3>
 * <ul>
 *   <li><strong>Core Pool Size:</strong> Number of available CPU cores</li>
 *   <li><strong>Maximum Pool Size:</strong> 2x number of available CPU cores</li>
 *   <li><strong>Scheduled Pool Size:</strong> Number of available CPU cores</li>
 * </ul>
 *
 * <h3>Configuration Properties:</h3>
 * <pre>
 * thread-pool:
 *   enabled: true
 * spring:
 *   task:
 *     execution:
 *       pool:
 *         core-size: 8
 *         max-size: 16
 *         queue-capacity: 100
 *         keep-alive: 60s
 *         allow-core-thread-timeout: true
 *       thread-name-prefix: "async-"
 * </pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see ThreadPoolTaskExecutor
 * @see ScheduledExecutorService
 * @see TaskExecutionProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class ThreadPoolConfig {

    /**
     * Number of available CPU cores, used as basis for thread pool sizing.
     *
     * This value is determined at startup and used to calculate appropriate
     * thread pool sizes based on the system's processing capabilities.
     */
    private final int core = Runtime.getRuntime().availableProcessors();

    /**
     * Creates a configurable ThreadPoolTaskExecutor for asynchronous task execution.
     *
     * This method creates a ThreadPoolTaskExecutor that integrates with Spring Boot's
     * TaskExecutionProperties for flexible configuration. The executor is designed
     * for general-purpose asynchronous operations, particularly @Async annotated methods.
     *
     * <p><strong>Configuration Strategy:</strong>
     * <ul>
     *   <li>Uses properties from TaskExecutionProperties when available</li>
     *   <li>Falls back to CPU-based defaults when properties are not set</li>
     *   <li>Implements CallerRunsPolicy for rejection handling</li>
     *   <li>Supports core thread timeout for resource efficiency</li>
     * </ul>
     *
     * <p><strong>Thread Pool Parameters:</strong>
     * <ul>
     *   <li><strong>Core Pool Size:</strong> Configured value or CPU core count</li>
     *   <li><strong>Max Pool Size:</strong> Configured value or 2x CPU core count</li>
     *   <li><strong>Queue Capacity:</strong> From configuration properties</li>
     *   <li><strong>Keep Alive:</strong> From configuration properties</li>
     *   <li><strong>Thread Name Prefix:</strong> From configuration properties</li>
     * </ul>
     *
     * <p><strong>Rejection Policy:</strong>
     * Uses CallerRunsPolicy which executes rejected tasks in the calling thread,
     * providing graceful degradation under high load conditions.
     *
     * @param threadPoolProperties Spring Boot's task execution properties for configuration
     * @return Configured ThreadPoolTaskExecutor for asynchronous task execution
     *
     * @see ThreadPoolTaskExecutor
     * @see TaskExecutionProperties
     * @see ThreadPoolExecutor.CallerRunsPolicy
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
     * Creates a ScheduledExecutorService for periodic and delayed task execution.
     *
     * This method creates a ScheduledThreadPoolExecutor specifically designed for
     * scheduled tasks such as @Scheduled annotated methods, periodic maintenance
     * operations, and delayed task execution.
     *
     * <p><strong>Executor Configuration:</strong>
     * <ul>
     *   <li><strong>Core Pool Size:</strong> Equal to CPU core count</li>
     *   <li><strong>Thread Factory:</strong> Creates daemon threads with meaningful names</li>
     *   <li><strong>Rejection Policy:</strong> CallerRunsPolicy for graceful degradation</li>
     *   <li><strong>Exception Handling:</strong> Custom afterExecute for error handling</li>
     * </ul>
     *
     * <p><strong>Thread Characteristics:</strong>
     * <ul>
     *   <li><strong>Daemon Threads:</strong> Won't prevent JVM shutdown</li>
     *   <li><strong>Named Threads:</strong> "schedule-pool-N" pattern for identification</li>
     *   <li><strong>Exception Handling:</strong> Overridden afterExecute for error processing</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong>
     * <ul>
     *   <li>@Scheduled annotated methods</li>
     *   <li>Periodic maintenance tasks</li>
     *   <li>Delayed task execution</li>
     *   <li>Timeout operations</li>
     * </ul>
     *
     * @return Configured ScheduledExecutorService for scheduled task execution
     *
     * @see ScheduledThreadPoolExecutor
     * @see BasicThreadFactory
     * @see ThreadPoolExecutor.CallerRunsPolicy
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(core,
                new BasicThreadFactory.Builder()
                        .namingPattern("schedule-pool-%d")
                        .daemon(true)
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {

            /**
             * Custom afterExecute implementation for exception handling in scheduled tasks.
             *
             * This method is called after each task execution and provides an opportunity
             * to handle exceptions that occurred during task execution. Currently, it
             * delegates to the parent implementation and includes a placeholder for
             * custom exception handling.
             *
             * @param r The runnable that has completed execution
             * @param t The exception that caused execution to terminate, or null if execution completed normally
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                // TODO: Implement custom exception handling when SpringThreadUtils is available
                // SpringThreadUtils.printException(r, t);
            }
        };
    }
}
