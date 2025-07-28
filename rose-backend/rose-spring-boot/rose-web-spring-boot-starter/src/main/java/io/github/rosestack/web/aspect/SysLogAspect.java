package io.github.rosestack.web.aspect;

import io.github.rosestack.core.util.ServletUtils;
import io.github.rosestack.web.annotation.SysLog;
import io.github.rosestack.web.config.WebProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志切面
 * <p>
 * 处理 @SysLog 注解，记录操作日志
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SysLogAspect {
    private final WebProperties webProperties;

    @Around("@annotation(io.github.rosestack.web.annotation.SysLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLog sysLog = method.getAnnotation(SysLog.class);

        // 记录开始日志
        String operationType = sysLog.type();
        String description = sysLog.description();
        String module = sysLog.module();
        String requestId = ServletUtils.getHeader(webProperties.getFilter().getRequestId().getHeaderName());
        String clientIp = ServletUtils.getClientIp();
        String userAgent = ServletUtils.getUserAgent();

        log.info("操作开始 - 类型: {}, 描述: {}, 模块: {}, 请求ID: {}, 客户端IP: {}, User-Agent: {}",
                operationType, description, module, requestId, clientIp, userAgent);

        // 记录请求参数
        if (sysLog.logParams()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                log.debug("请求参数: {}", args);
            }
        }

        // 执行目标方法
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            stopWatch.stop();

            // 记录执行时间
            if (sysLog.logExecutionTime()) {
                log.info("操作完成 - 类型: {}, 描述: {}, 执行时间: {}ms, 请求ID: {}",
                        operationType, description, stopWatch.getTotalTimeMillis(), requestId);
            }

            // 记录返回结果
            if (sysLog.logResult()) {
                log.debug("返回结果: {}", result);
            }

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            exception = e;

            // 记录异常信息
            log.error("操作异常 - 类型: {}, 描述: {}, 执行时间: {}ms, 请求ID: {}, 异常: {}",
                    operationType, description, stopWatch.getTotalTimeMillis(), requestId, e.getMessage(), e);

            throw e;
        } finally {
            // 异步记录详细日志
            if (sysLog.async()) {
                Object finalResult = result;
                Exception finalException = exception;
                CompletableFuture.runAsync(() -> {
                    try {
                        logDetailedOperation(sysLog, method, finalResult, finalException, stopWatch.getTotalTimeMillis());
                    } catch (Exception e) {
                        log.warn("异步记录操作日志失败", e);
                    }
                });
            } else {
                logDetailedOperation(sysLog, method, result, exception, stopWatch.getTotalTimeMillis());
            }
        }
    }

    /**
     * 记录详细操作日志
     */
    private void logDetailedOperation(SysLog sysLog, Method method,
                                      Object result, Exception exception, long executionTime) {

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("详细操作日志:\n");
        logBuilder.append("  操作类型: ").append(sysLog.type()).append("\n");
        logBuilder.append("  操作描述: ").append(sysLog.description()).append("\n");
        logBuilder.append("  模块: ").append(sysLog.module()).append("\n");
        logBuilder.append("  方法: ").append(method.getDeclaringClass().getSimpleName()).append(".").append(method.getName()).append("\n");
        logBuilder.append("  执行时间: ").append(executionTime).append("ms\n");

        HttpServletRequest request = ServletUtils.getCurrentRequest();
        if (request != null) {
            logBuilder.append("  请求URL: ").append(request.getRequestURI()).append("\n");
            logBuilder.append("  请求方法: ").append(request.getMethod()).append("\n");
            logBuilder.append("  User-Agent: ").append(ServletUtils.getUserAgent()).append("\n");
        }

        if (sysLog.logUser()) {
            // TODO: 从安全上下文获取当前用户信息
            logBuilder.append("  操作用户: ").append("待实现").append("\n");
        }

        if (sysLog.logIp()) {
            logBuilder.append("  客户端IP: ").append(ServletUtils.getClientIp()).append("\n");
        }

        if (exception != null) {
            logBuilder.append("  异常信息: ").append(exception.getMessage()).append("\n");
        }

        log.info(logBuilder.toString());
    }
} 