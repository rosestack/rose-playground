package io.github.rosestack.infrastructure.exception;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.io.IOException;
import java.util.UUID;

/**
 * 响应头过滤器
 * 统一添加请求追踪、响应时间、重试信息等响应头
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class ResponseHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 生成请求ID
        String requestId = generateRequestId();
        MDC.put("requestId", requestId);

        try {
            // 继续处理请求
            chain.doFilter(request, response);

        } finally {
            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;

            // 添加通用响应头
            addCommonHeaders(httpResponse, requestId, responseTime);

            // 如果是错误响应，添加错误相关响应头
            if (httpResponse.getStatus() >= 400) {
                addErrorHeaders(httpResponse, httpRequest);
            }

            // 清理MDC
            MDC.clear();
        }
    }

    /**
     * 添加通用响应头
     */
    private void addCommonHeaders(HttpServletResponse response, String requestId, long responseTime) {
        response.setHeader("X-Request-Id", requestId);
        response.setHeader("X-Response-Time", responseTime + "ms");
        response.setHeader("X-Server-Time", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 添加错误响应头
     */
    private void addErrorHeaders(HttpServletResponse response, HttpServletRequest request) {
        // 从请求属性中获取异常信息（由异常处理器设置）
        Exception exception = (Exception) request.getAttribute("exception");

        if (exception != null) {
            // 设置错误类型
            String errorType = getErrorType(exception);
            response.setHeader("X-Error-Type", errorType);

            // 设置是否可重试
            boolean retryable = isRetryable(exception);
            response.setHeader("X-Retry-Allowed", String.valueOf(retryable));

            // 设置错误详情
            String errorDetails = getExceptionCauseMessage(exception);
            response.setHeader("X-Error-Details", errorDetails);

            // 如果是限流异常，设置重试间隔
            if (exception instanceof RateLimitException) {
                RateLimitException rateLimitException = (RateLimitException) exception;
                response.setHeader("Retry-After", String.valueOf(rateLimitException.getRetryAfterSeconds()));
            }
        }
    }

    /**
     * 获取错误类型
     */
    private String getErrorType(Exception e) {
        if (e instanceof RateLimitException) {
            return "RATE_LIMIT";
        } else if (e instanceof BusinessException) {
            return "BUSINESS";
        } else if (isClientError(e)) {
            return "CLIENT";
        } else {
            return "SYSTEM";
        }
    }

    /**
     * 判断异常是否可重试
     */
    public boolean isRetryable(Exception e) {
        return e instanceof RateLimitException ||
                (e instanceof Exception && !isClientError(e));
    }

    /**
     * 判断是否为客户端错误
     */
    private boolean isClientError(Exception e) {
        return e instanceof MethodArgumentNotValidException ||
                e instanceof BindException ||
                e instanceof HttpMessageNotReadableException ||
                e instanceof MissingServletRequestParameterException ||
                e instanceof AuthenticationException ||
                e instanceof BadCredentialsException ||
                e instanceof AccessDeniedException ||
                e instanceof BusinessException;
    }

    /**
     * 获取异常cause的message信息
     */
    private String getExceptionCauseMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause.getMessage();
        }
        return e.getMessage();
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}