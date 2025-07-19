package io.github.rose.core.util;

import io.vavr.control.Try;
import io.vavr.Function1;
import io.vavr.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 增强的弹性模式工具类
 * 提供断路器、指数退避重试、超时控制等高级功能
 * 
 * @author rose
 */
public class ResilienceUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ResilienceUtils.class);
    
    // ==================== 断路器实现 ====================
    
    /**
     * 简单断路器
     */
    public static class CircuitBreaker {
        private final int failureThreshold;
        private final Duration resetTimeout;
        private final AtomicInteger failureCount;
        private final AtomicLong lastFailureTime;
        private volatile CircuitState state;
        
        public enum CircuitState {
            CLOSED,     // 正常状态
            OPEN,       // 开启状态（快速失败）
            HALF_OPEN   // 半开状态（尝试恢复）
        }
        
        public CircuitBreaker(int failureThreshold, Duration resetTimeout) {
            this.failureThreshold = failureThreshold;
            this.resetTimeout = resetTimeout;
            this.failureCount = new AtomicInteger(0);
            this.lastFailureTime = new AtomicLong(0);
            this.state = CircuitState.CLOSED;
        }
        
        public <T> Try<T> execute(Supplier<Try<T>> operation) {
            if (state == CircuitState.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime.get() > resetTimeout.toMillis()) {
                    state = CircuitState.HALF_OPEN;
                    log.info("Circuit breaker transitioning to HALF_OPEN");
                } else {
                    return Try.failure(new RuntimeException("Circuit breaker is OPEN"));
                }
            }
            
            Try<T> result = operation.get();
            
            if (result.isSuccess()) {
                onSuccess();
            } else {
                onFailure();
            }
            
            return result;
        }
        
        private void onSuccess() {
            if (state == CircuitState.HALF_OPEN) {
                state = CircuitState.CLOSED;
                failureCount.set(0);
                log.info("Circuit breaker reset to CLOSED");
            }
        }
        
        private void onFailure() {
            failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            
            if (state == CircuitState.CLOSED && failureCount.get() >= failureThreshold) {
                state = CircuitState.OPEN;
                log.warn("Circuit breaker opened after {} failures", failureCount.get());
            } else if (state == CircuitState.HALF_OPEN) {
                state = CircuitState.OPEN;
                log.warn("Circuit breaker reopened in HALF_OPEN state");
            }
        }
        
        public CircuitState getState() {
            return state;
        }
        
        public int getFailureCount() {
            return failureCount.get();
        }
    }
    
    // ==================== 指数退避重试 ====================
    
    /**
     * 指数退避重试策略
     */
    public static class ExponentialBackoffRetry {
        private final int maxAttempts;
        private final Duration initialDelay;
        private final double multiplier;
        private final Duration maxDelay;
        private final Predicate<Throwable> retryCondition;
        
        public ExponentialBackoffRetry(int maxAttempts, Duration initialDelay, 
                                     double multiplier, Duration maxDelay,
                                     Predicate<Throwable> retryCondition) {
            this.maxAttempts = maxAttempts;
            this.initialDelay = initialDelay;
            this.multiplier = multiplier;
            this.maxDelay = maxDelay;
            this.retryCondition = retryCondition;
        }
        
        public <T> Try<T> execute(Supplier<Try<T>> operation) {
            Try<T> result = operation.get();
            int attempts = 1;
            
            while (result.isFailure() && attempts < maxAttempts) {
                Throwable cause = result.getCause();
                
                if (!retryCondition.test(cause)) {
                    log.debug("Not retrying due to condition: {}", cause.getClass().getSimpleName());
                    break;
                }
                
                long delay = calculateDelay(attempts);
                log.warn("Operation failed, retrying in {}ms (attempt {}/{})", delay, attempts + 1, maxAttempts);
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Try.failure(e);
                }
                
                result = operation.get();
                attempts++;
            }
            
            if (result.isFailure()) {
                log.error("Operation failed after {} attempts", attempts);
            }
            
            return result;
        }
        
        private long calculateDelay(int attempt) {
            long delay = (long) (initialDelay.toMillis() * Math.pow(multiplier, attempt - 1));
            return Math.min(delay, maxDelay.toMillis());
        }
    }
    
    // ==================== 组合策略 ====================
    
    /**
     * 组合多种弹性策略
     */
    public static class CompositeResilience {
        private final CircuitBreaker circuitBreaker;
        private final ExponentialBackoffRetry retry;
        private final Duration timeout;
        
        public CompositeResilience(CircuitBreaker circuitBreaker, 
                                 ExponentialBackoffRetry retry, 
                                 Duration timeout) {
            this.circuitBreaker = circuitBreaker;
            this.retry = retry;
            this.timeout = timeout;
        }
        
        public <T> Try<T> execute(Supplier<Try<T>> operation) {
            return circuitBreaker.execute(() -> 
                retry.execute(() -> 
                    withTimeout(operation, timeout)
                )
            );
        }
        
        private <T> Try<T> withTimeout(Supplier<Try<T>> operation, Duration timeout) {
            return Try.of(() -> {
                long startTime = System.currentTimeMillis();
                Try<T> result = operation.get();
                
                if (System.currentTimeMillis() - startTime > timeout.toMillis()) {
                    throw new RuntimeException("Operation timed out after " + timeout.toMillis() + "ms");
                }
                
                if (result.isFailure()) {
                    throw result.getCause();
                }
                
                return result.get();
            });
        }
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 创建默认的断路器
     */
    public static CircuitBreaker createCircuitBreaker() {
        return new CircuitBreaker(5, Duration.ofMinutes(1));
    }
    
    /**
     * 创建默认的指数退避重试
     */
    public static ExponentialBackoffRetry createExponentialBackoffRetry() {
        return new ExponentialBackoffRetry(
            3, 
            Duration.ofSeconds(1), 
            2.0, 
            Duration.ofSeconds(30),
            throwable -> throwable instanceof RuntimeException
        );
    }
    
    /**
     * 创建组合策略
     */
    public static CompositeResilience createCompositeResilience() {
        return new CompositeResilience(
            createCircuitBreaker(),
            createExponentialBackoffRetry(),
            Duration.ofSeconds(10)
        );
    }
    
    // ==================== 函数式接口支持 ====================
    
    /**
     * 带断路器的 CheckedConsumer
     */
    public static <T> Function1<T, Try<Void>> withCircuitBreaker(
            Function1<T, Try<Void>> consumer, CircuitBreaker circuitBreaker) {
        return value -> circuitBreaker.execute(() -> consumer.apply(value));
    }
    
    /**
     * 带指数退避重试的 CheckedConsumer
     */
    public static <T> Function1<T, Try<Void>> withExponentialBackoff(
            Function1<T, Try<Void>> consumer, ExponentialBackoffRetry retry) {
        return value -> retry.execute(() -> consumer.apply(value));
    }
    
    /**
     * 带组合策略的 CheckedConsumer
     */
    public static <T> Function1<T, Try<Void>> withCompositeResilience(
            Function1<T, Try<Void>> consumer, CompositeResilience resilience) {
        return value -> resilience.execute(() -> consumer.apply(value));
    }
    
    // ==================== 实际应用示例 ====================
    
    /**
     * 网络请求的弹性处理示例
     */
    public static class NetworkResilienceExample {
        
        private final CompositeResilience resilience;
        
        public NetworkResilienceExample() {
            // 创建专门针对网络请求的弹性策略
            CircuitBreaker circuitBreaker = new CircuitBreaker(3, Duration.ofMinutes(2));
            ExponentialBackoffRetry retry = new ExponentialBackoffRetry(
                5, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(60),
                throwable -> isRetryableNetworkException(throwable)
            );
            this.resilience = new CompositeResilience(circuitBreaker, retry, Duration.ofSeconds(30));
        }
        
        public <T> Try<T> executeNetworkCall(Supplier<Try<T>> networkOperation) {
            return resilience.execute(networkOperation);
        }
        
        private boolean isRetryableNetworkException(Throwable throwable) {
            String message = throwable.getMessage();
            return message != null && (
                message.contains("Connection refused") ||
                message.contains("timeout") ||
                message.contains("Network is unreachable") ||
                message.contains("500") ||
                message.contains("502") ||
                message.contains("503") ||
                message.contains("504")
            );
        }
    }
} 