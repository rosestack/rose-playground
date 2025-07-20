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
 * Asynchronous execution configuration for Spring's @Async annotation support.
 * <p>
 * This configuration class provides centralized setup for asynchronous method execution
 * in the application. It implements Spring's AsyncConfigurer interface to customize
 * the default behavior of @Async annotated methods, including thread pool configuration
 * and exception handling strategies.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Custom Thread Pool:</strong> Uses a configured ThreadPoolTaskExecutor for async operations</li>
 *   <li><strong>Exception Handling:</strong> Provides comprehensive error handling for async method failures</li>
 *   <li><strong>Proxy Configuration:</strong> Enables class-based proxying for better compatibility</li>
 *   <li><strong>Auto-Configuration:</strong> Automatically applied when present on classpath</li>
 * </ul>
 *
 * <h3>Thread Pool Integration:</h3>
 * The configuration integrates with a pre-configured ThreadPoolTaskExecutor bean,
 * ensuring that all @Async methods use a consistent, properly tuned thread pool
 * rather than the default SimpleAsyncTaskExecutor.
 *
 * <h3>Exception Handling Strategy:</h3>
 * Uncaught exceptions in async methods are handled by converting them into
 * BusinessException instances with detailed context information including:
 * - Original exception message
 * - Method name where the exception occurred
 * - Parameter values passed to the method
 *
 * <h3>Usage:</h3>
 * Once this configuration is active, any method annotated with @Async will:
 * <pre>{@code
 * @Service
 * public class MyService {
 *
 *     @Async
 *     public CompletableFuture<String> processAsync(String data) {
 *         // This method will execute in the configured thread pool
 *         return CompletableFuture.completedFuture("Processed: " + data);
 *     }
 * }
 * }</pre>
 *
 * <h3>Configuration Dependencies:</h3>
 * This configuration requires a ThreadPoolTaskExecutor bean named "threadPoolTaskExecutor"
 * to be available in the application context. This is typically provided by ThreadPoolConfig.
 *
 * @author Rose Framework Team
 * @see AsyncConfigurer
 * @see EnableAsync
 * @see ThreadPoolTaskExecutor
 * @see AsyncUncaughtExceptionHandler
 * @since 1.0.0
 */
@EnableAsync(proxyTargetClass = true)
@AutoConfiguration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    /**
     * The thread pool executor used for asynchronous method execution.
     * <p>
     * This executor is injected by qualifier to ensure we get the specific
     * ThreadPoolTaskExecutor configured for the application rather than
     * any other Executor that might be available in the context.
     */
    @Qualifier("threadPoolTaskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * Provides the custom executor for @Async annotated methods.
     * <p>
     * This method overrides the default Spring async executor configuration
     * to use our custom ThreadPoolTaskExecutor instead of the default
     * SimpleAsyncTaskExecutor. This provides better control over thread
     * pool sizing, naming, and resource management.
     *
     * <p><strong>Benefits of Custom Executor:</strong>
     * <ul>
     *   <li>Controlled thread pool size and queue capacity</li>
     *   <li>Meaningful thread names for debugging</li>
     *   <li>Proper resource cleanup and lifecycle management</li>
     *   <li>Integration with application monitoring and metrics</li>
     * </ul>
     *
     * @return The configured ThreadPoolTaskExecutor for async method execution
     * @see ThreadPoolTaskExecutor
     * @see AsyncConfigurer#getAsyncExecutor()
     */
    @Override
    public Executor getAsyncExecutor() {
        return threadPoolTaskExecutor;
    }

    /**
     * Provides a custom exception handler for uncaught exceptions in async methods.
     * <p>
     * This handler implements a comprehensive error handling strategy for async methods
     * that fail with uncaught exceptions. Since async methods run in separate threads,
     * normal exception propagation doesn't work, so this handler ensures that errors
     * are properly captured and converted into meaningful business exceptions.
     *
     * <p><strong>Exception Handling Process:</strong>
     * <ol>
     *   <li>Print stack trace for debugging purposes</li>
     *   <li>Extract method name and parameter information</li>
     *   <li>Build detailed error message with context</li>
     *   <li>Throw BusinessException with comprehensive error details</li>
     * </ol>
     *
     * <p><strong>Error Information Captured:</strong>
     * <ul>
     *   <li>Original exception message</li>
     *   <li>Method name where exception occurred</li>
     *   <li>Parameter values passed to the method (if any)</li>
     * </ul>
     *
     * <p><strong>Important Note:</strong>
     * The BusinessException thrown by this handler will not propagate to the
     * calling thread since async methods run independently. This handler is
     * primarily for logging and monitoring purposes. Consider using
     * CompletableFuture return types for better error handling in async methods.
     *
     * @return AsyncUncaughtExceptionHandler that converts exceptions to BusinessException
     * @see AsyncUncaughtExceptionHandler
     * @see BusinessException
     * @see AsyncConfigurer#getAsyncUncaughtExceptionHandler()
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
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
