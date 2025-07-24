package io.github.rose.infrastructure.config;

import io.github.rose.infrastructure.exception.BusinessException;
import io.github.rose.infrastructure.exception.RateLimitException;
import io.github.rose.interfaces.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.stream.Collectors;


/**
 * 全局异常处理器
 * <p>
 * 统一处理应用程序中的各种异常，转换为标准的 HTTP 响应格式，支持国际化错误消息。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>统一异常响应格式</li>
 *   <li>支持国际化错误消息</li>
 *   <li>根据异常类型返回相应 HTTP 状态码</li>
 * </ul>
 * <p>
 * <h3>处理的异常类型：</h3>
 * <ul>
 *   <li>BusinessException - 业务异常，返回 400</li>
 *   <li>RateLimitException - 限流异常，返回 429</li>
 *   <li>MethodArgumentNotValidException - 参数验证异常，返回 400</li>
 *   <li>RuntimeException - 运行时异常，返回 500</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    /**
     * 请求追踪ID
     */
    public static final String REQUEST_ID = "X-Request-ID";

    /**
     * 响应时间戳
     */
    public static final String RESPONSE_TIME = "X-Response-Time";

    /**
     * 是否可重试
     */
    public static final String RETRY_ALLOWED = "X-Retry-Allowed";

    /**
     * 重试间隔（标准头）
     */
    public static final String RETRY_AFTER = "Retry-After";

    /**
     * 错误详情
     */
    public static final String ERROR_DETAILS = "X-Error-Details";

    /**
     * 错误类型：
     * CLIENT,     // 客户端错误
     * BUSINESS,   // 业务错误
     * SYSTEM,     // 系统错误
     * RATE_LIMIT  // 限流错误
     */
    public static final String ERROR_TYPE = "X-Error-Type";

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e,
            HttpServletRequest request,
            Locale locale) {
        String localizedMessage = getLocalizedMessage(e.getMessageKey(), e.getMessageArgs(), locale);

        log.warn("业务异常: {} - {} - {}", request.getRequestURI(), e.getMessageKey(), localizedMessage);

        ApiResponse<Void> response = ApiResponse.error(500, localizedMessage);

        return ResponseEntity.status(500)
                .header(ERROR_DETAILS, getExceptionCauseMessage(e))
                .header(REQUEST_ID, getRequestId())
                .header(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()))
                .header(ERROR_TYPE, "BUSINESS")
                .header(RETRY_ALLOWED, Boolean.FALSE.toString())
                .body(response);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
            RateLimitException e,
            HttpServletRequest request,
            Locale locale) {
        String localizedMessage = getLocalizedMessage(e.getMessageKey(), e.getMessageArgs(), locale);

        log.warn("限流异常: {} - {} - {}", request.getRequestURI(), e.getMessageKey(), localizedMessage);

        ApiResponse<Void> response = ApiResponse.error(429, localizedMessage);

        return ResponseEntity.status(429)
                .header(ERROR_DETAILS, getExceptionCauseMessage(e))
                .header(REQUEST_ID, getRequestId())
                .header(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()))
                .header(ERROR_TYPE, "RATE_LIMIT")
                .header(RETRY_ALLOWED, Boolean.TRUE.toString())
                .header(RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request,
            Locale locale) {

        String localizedMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String fieldName = error.getField();
                    String defaultMessage = error.getDefaultMessage();
                    try {
                        return messageSource.getMessage(
                                "validation." + fieldName + "." + error.getCode(),
                                error.getArguments(),
                                defaultMessage,
                                locale
                        );
                    } catch (NoSuchMessageException ex) {
                        return defaultMessage;
                    }
                })
                .collect(Collectors.joining(", "));

        log.warn("参数验证失败: {} - {}", request.getRequestURI(), localizedMessage);

        ApiResponse<Void> response = ApiResponse.error(400, localizedMessage);

        return ResponseEntity.status(400)
                .header(ERROR_DETAILS, getExceptionCauseMessage(e))
                .header(REQUEST_ID, getRequestId())
                .header(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()))
                .header(ERROR_TYPE, "CLIENT")
                .header(RETRY_ALLOWED, Boolean.FALSE.toString())
                .body(response);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception e,
            HttpServletRequest request,
            Locale locale) {
        String localizedMessage = getLocalizedMessage("auth.error.authentication_failed", null, locale);
        log.warn("认证失败: {} - {}", request.getRequestURI(), localizedMessage);

        ApiResponse<Void> response = ApiResponse.error(401, localizedMessage);

        return ResponseEntity.status(401)
                .header(ERROR_DETAILS, getExceptionCauseMessage(e))
                .header(REQUEST_ID, getRequestId())
                .header(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()))
                .header(ERROR_TYPE, "CLIENT")
                .header(RETRY_ALLOWED, Boolean.FALSE.toString())
                .body(response);
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request,
            Locale locale) {
        String localizedMessage = getLocalizedMessage("auth.error.access_denied", null, locale);
        log.warn("访问被拒绝: {} - {}", request.getRequestURI(), localizedMessage);

        ApiResponse<Void> response = ApiResponse.error(403, localizedMessage);

        return ResponseEntity.status(403)
                .header(ERROR_DETAILS, getExceptionCauseMessage(e))
                .header(REQUEST_ID, getRequestId())
                .header(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()))
                .header(ERROR_TYPE, "CLIENT")
                .header(RETRY_ALLOWED, Boolean.FALSE.toString())
                .body(response);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception e,
            HttpServletRequest request,
            Locale locale) {
        String localizedMessage = getLocalizedMessage("common.error.internal_server", null, locale);

        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error(500, localizedMessage);

        return ResponseEntity.status(500)
                .header(ERROR_DETAILS, getExceptionCauseMessage(e))
                .header(REQUEST_ID, getRequestId())
                .header(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()))
                .header(ERROR_TYPE, "CLIENT")
                .header(RETRY_ALLOWED, Boolean.TRUE.toString())
                .body(response);
    }

    /**
     * 获取国际化消息
     */
    private String getLocalizedMessage(String messageKey, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(messageKey, args, messageKey, locale);
        } catch (NoSuchMessageException e) {
            log.warn("未找到国际化消息: {}", messageKey);
            return messageKey;
        }
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

    private String getRequestId() {
        return MDC.get(REQUEST_ID);
    }
}
