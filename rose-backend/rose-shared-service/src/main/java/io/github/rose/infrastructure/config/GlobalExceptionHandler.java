package io.github.rose.infrastructure.config;

import io.github.rose.infrastructure.exception.BusinessException;
import io.github.rose.infrastructure.exception.RateLimitException;
import io.github.rose.interfaces.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

import static io.github.rose.infrastructure.constants.HeaderConstants.*;


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
    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
        String localizedMessage = getLocalizedMessage(e.getMessageKey(), e.getMessageArgs());
        log.info("业务异常: {} - {}", e.getMessageKey(), localizedMessage);

        // 设置响应头
        setResponseHeaders(response, e.getMessage(), false, request);

        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), localizedMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(RateLimitException e, HttpServletRequest request, HttpServletResponse response) {
        String localizedMessage = getLocalizedMessage(e.getMessageKey(), e.getMessageArgs());
        log.warn("限流异常: {} - {}, 重试间隔: {}秒", e.getMessageKey(), localizedMessage, e.getRetryAfter());

        // 设置响应头
        setResponseHeaders(response, e.getMessage(), true, request);
        response.setHeader(RETRY_AFTER, String.valueOf(e.getRetryAfter()));

        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), localizedMessage);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(apiResponse);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        StringBuilder errorMessage = new StringBuilder();

        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) e;
            for (FieldError fieldError : validException.getBindingResult().getFieldErrors()) {
                errorMessage.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
            }
        } else if (e instanceof BindException) {
            BindException bindException = (BindException) e;
            for (FieldError fieldError : bindException.getBindingResult().getFieldErrors()) {
                errorMessage.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
            }
        }

        String message = errorMessage.length() > 0 ? errorMessage.toString() : "参数验证失败";
        log.warn("参数验证异常: {}", message);

        // 设置响应头
        setResponseHeaders(response, e.getMessage(), false, request);

        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        String localizedMessage = getLocalizedMessage("common.error.internal_server");
        log.error("系统异常: {} - {}", e.getMessage(), localizedMessage, e);

        // 设置响应头
        setResponseHeaders(response, e.getMessage(), true, request);

        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), localizedMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    private void setResponseHeaders(HttpServletResponse response, String errorDetails,
                                    boolean retryable, HttpServletRequest request) {
        response.setHeader(REQUEST_ID, MDC.get(REQUEST_ID));
        response.setHeader(RESPONSE_TIME, String.valueOf(System.currentTimeMillis()));
        response.setHeader(ERROR_DETAILS, errorDetails);
        response.setHeader(RETRY_ALLOWED, String.valueOf(retryable));
    }

    /**
     * 获取国际化消息
     *
     * @param messageKey 消息键
     * @param args       消息参数
     * @return 国际化消息
     */
    private String getLocalizedMessage(String messageKey, Object... args) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(messageKey, args, locale);
        } catch (Exception e) {
            log.warn("获取国际化消息失败: {}", messageKey, e);
            return messageKey;
        }
    }
}
