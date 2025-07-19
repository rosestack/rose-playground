package io.github.rose.core.lang.function;

import io.vavr.control.Try;
import io.vavr.Function0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Vavr Runnable 工具类
 * 提供对 Vavr Function0 和标准 Runnable 的工具方法和转换功能
 * 
 * @author rose
 */
public class VavrCheckedRunnable {
    
    private static final Logger log = LoggerFactory.getLogger(VavrCheckedRunnable.class);
    
    // ==================== 转换方法 ====================
    
    /**
     * 将标准 Runnable 转换为 Vavr Function0
     * 
     * @param runnable 标准可运行对象
     * @return Vavr Function0
     */
    public static Function0<Void> toFunction0(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        return () -> {
            runnable.run();
            return null;
        };
    }
    
    /**
     * 将标准 Runnable 转换为 Vavr Function0<Try<Void>>
     * 
     * @param runnable 标准可运行对象
     * @return Vavr Function0<Try<Void>>
     */
    public static Function0<Try<Void>> toFunction0Try(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        return () -> Try.of(() -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * 将 Vavr Function0 转换为标准 Runnable
     * 
     * @param function Vavr 函数
     * @return 标准 Runnable
     */
    public static Runnable toRunnable(Function0<Void> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return () -> function.apply();
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 执行 Runnable 并返回 Try
     * 
     * @param runnable 可运行对象
     * @return Try 结果
     */
    public static Try<Void> execute(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        return Try.of(() -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * 执行 Vavr Function0 并返回 Try
     * 
     * @param function Vavr 函数
     * @return Try 结果
     */
    public static Try<Void> execute(Function0<Void> function) {
        Objects.requireNonNull(function, "function cannot be null");
        return Try.of(() -> {
            function.apply();
            return null;
        });
    }
    
    // ==================== 装饰器方法 ====================
    
    /**
     * 创建带重试的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param maxRetries 最大重试次数
     * @return 带重试的 Runnable
     */
    public static Runnable withRetry(Runnable runnable, int maxRetries) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        
        return () -> {
            Try<Void> result = Try.of(() -> {
                runnable.run();
                return null;
            });
            int retries = 0;
            
            while (result.isFailure() && retries < maxRetries) {
                log.warn("Runnable failed, retrying... (attempt {}/{})", retries + 1, maxRetries);
                result = Try.of(() -> {
                    runnable.run();
                    return null;
                });
                retries++;
            }
            
            if (result.isFailure()) {
                throw new RuntimeException(result.getCause());
            }
        };
    }
    
    /**
     * 创建带超时的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param timeoutMs 超时时间（毫秒）
     * @return 带超时的 Runnable
     */
    public static Runnable withTimeout(Runnable runnable, long timeoutMs) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        
        return () -> {
            try {
                CompletableFuture<Void> future = CompletableFuture.runAsync(runnable);
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * 创建带日志的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param operationName 操作名称
     * @return 带日志的 Runnable
     */
    public static Runnable withLogging(Runnable runnable, String operationName) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return () -> {
            log.debug("Starting runnable operation: {}", operationName);
            
            try {
                runnable.run();
                log.debug("Runnable operation {} completed successfully", operationName);
            } catch (Exception e) {
                log.error("Runnable operation {} failed: {}", operationName, e.getMessage());
                throw e;
            }
        };
    }
    
    /**
     * 创建带降级的 Runnable
     * 
     * @param primary 主要可运行对象
     * @param fallback 降级可运行对象
     * @return 带降级的 Runnable
     */
    public static Runnable withFallback(Runnable primary, Runnable fallback) {
        Objects.requireNonNull(primary, "primary runnable cannot be null");
        Objects.requireNonNull(fallback, "fallback runnable cannot be null");
        
        return () -> {
            try {
                primary.run();
            } catch (Exception e) {
                log.warn("Primary runnable failed, using fallback: {}", e.getMessage());
                fallback.run();
            }
        };
    }
    
    /**
     * 创建带异常处理的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param exceptionHandler 异常处理器
     * @return 带异常处理的 Runnable
     */
    public static Runnable withExceptionHandling(Runnable runnable, Function0<Void> exceptionHandler) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                exceptionHandler.apply();
            }
        };
    }
    
    /**
     * 创建带指标的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param metricsCollector 指标收集器
     * @param operationName 操作名称
     * @return 带指标的 Runnable
     */
    public static Runnable withMetrics(Runnable runnable,
                                      Function0<Void> metricsCollector,
                                      String operationName) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        Objects.requireNonNull(metricsCollector, "metricsCollector cannot be null");
        Objects.requireNonNull(operationName, "operationName cannot be null");
        
        return () -> {
            long startTime = System.currentTimeMillis();
            try {
                runnable.run();
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply();
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                metricsCollector.apply();
                throw e;
            }
        };
    }
    
    // ==================== 组合方法 ====================
    
    /**
     * 条件执行 Runnable
     * 
     * @param condition 条件
     * @param runnable 可运行对象
     */
    public static void conditional(boolean condition, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        
        if (condition) {
            runnable.run();
        }
    }
    
    /**
     * 批量执行 Runnable
     * 
     * @param runnables 可运行对象列表
     */
    @SafeVarargs
    public static void forEach(Runnable... runnables) {
        Objects.requireNonNull(runnables, "runnables cannot be null");
        if (runnables.length == 0) {
            throw new IllegalArgumentException("runnables array cannot be empty");
        }
        
        for (Runnable runnable : runnables) {
            Objects.requireNonNull(runnable, "runnable in array cannot be null");
            runnable.run();
        }
    }
    
    /**
     * 批量执行 Runnable，收集所有结果（包括失败）
     * 
     * @param runnables 可运行对象列表
     * @return Try 结果列表
     */
    @SafeVarargs
    public static java.util.List<Try<Void>> forEachCollect(Runnable... runnables) {
        Objects.requireNonNull(runnables, "runnables cannot be null");
        
        return java.util.Arrays.stream(runnables)
                .map(runnable -> Try.of(() -> {
                    runnable.run();
                    return (Void) null;
                }))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 链式执行多个 Runnable
     * 
     * @param runnables 可运行对象列表
     */
    @SafeVarargs
    public static void chain(Runnable... runnables) {
        Objects.requireNonNull(runnables, "runnables cannot be null");
        if (runnables.length == 0) {
            throw new IllegalArgumentException("runnables array cannot be empty");
        }
        
        for (Runnable runnable : runnables) {
            Objects.requireNonNull(runnable, "runnable in array cannot be null");
            runnable.run();
        }
    }
    
    /**
     * 创建带异步的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @return 异步 Runnable
     */
    public static Runnable withAsync(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        
        return () -> CompletableFuture.runAsync(runnable);
    }
    
    /**
     * 创建带并行处理的 Runnable
     * 
     * @param runnables 可运行对象列表
     * @return 并行处理 Runnable
     */
    @SafeVarargs
    public static Runnable withParallel(Runnable... runnables) {
        Objects.requireNonNull(runnables, "runnables cannot be null");
        if (runnables.length == 0) {
            throw new IllegalArgumentException("runnables array cannot be empty");
        }
        
        return () -> {
            try {
                java.util.List<CompletableFuture<Void>> futures = java.util.Arrays.stream(runnables)
                        .parallel()
                        .map(CompletableFuture::runAsync)
                        .collect(java.util.stream.Collectors.toList());
                
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (Exception e) {
                throw new RuntimeException("Parallel execution failed", e);
            }
        };
    }
    
    /**
     * 创建带延迟的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param delayMs 延迟时间（毫秒）
     * @return 带延迟的 Runnable
     */
    public static Runnable withDelay(Runnable runnable, long delayMs) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must be non-negative");
        }
        
        return () -> {
            try {
                Thread.sleep(delayMs);
                runnable.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Operation was interrupted", e);
            }
        };
    }
    
    /**
     * 创建带重复执行的 Runnable
     * 
     * @param runnable 原始可运行对象
     * @param times 重复次数
     * @return 带重复执行的 Runnable
     */
    public static Runnable withRepeat(Runnable runnable, int times) {
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (times <= 0) {
            throw new IllegalArgumentException("times must be positive");
        }
        
        return () -> {
            for (int i = 0; i < times; i++) {
                runnable.run();
            }
        };
    }
} 