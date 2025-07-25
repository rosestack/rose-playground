package io.github.rose.infrastructure.exception;

import io.github.rose.interfaces.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 注意：响应头由 ResponseHeaderFilter 统一处理
 *
 * @author chensoul
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
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e, HttpServletRequest request, Locale locale) {
        String localizedMessage = getLocalizedMessage(e.getMessageKey(), e.getMessageArgs(), locale);
        log.warn("业务异常: {} - {} - {}", request.getRequestURI(), e.getMessageKey(), localizedMessage);

        // 将异常存储到请求属性中，供过滤器使用
        request.setAttribute("exception", e);

        ApiResponse<Void> response = ApiResponse.error(500, localizedMessage);
        return ResponseEntity.status(500).body(response);
    }


    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
            RateLimitException e, HttpServletRequest request, Locale locale) {
        String localizedMessage = getLocalizedMessage(e.getMessageKey(), e.getMessageArgs(), locale);
        log.warn("限流异常: {} - {} - {}", request.getRequestURI(), e.getMessageKey(), localizedMessage);

        // 将异常存储到请求属性中，供过滤器使用
        request.setAttribute("exception", e);

        ApiResponse<Void> response = ApiResponse.error(429, localizedMessage);
        return ResponseEntity.status(429).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request, Locale locale) {
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

        // 将异常存储到请求属性中，供过滤器使用
        request.setAttribute("exception", e);

        ApiResponse<Void> response = ApiResponse.error(400, localizedMessage);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception e, HttpServletRequest request, Locale locale) {
        String localizedMessage = getLocalizedMessage("auth.error.authentication_failed", null, locale);
        log.warn("认证失败: {} - {}", request.getRequestURI(), localizedMessage);

        // 将异常存储到请求属性中，供过滤器使用
        request.setAttribute("exception", e);

        ApiResponse<Void> response = ApiResponse.error(401, localizedMessage);
        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request, Locale locale) {
        String localizedMessage = getLocalizedMessage("auth.error.access_denied", null, locale);
        log.warn("访问被拒绝: {} - {}", request.getRequestURI(), localizedMessage);

        // 将异常存储到请求属性中，供过滤器使用
        request.setAttribute("exception", e);

        ApiResponse<Void> response = ApiResponse.error(403, localizedMessage);
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception e, HttpServletRequest request, Locale locale) {
        String localizedMessage = getLocalizedMessage("common.error.internal_server", null, locale);
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        // 将异常存储到请求属性中，供过滤器使用
        request.setAttribute("exception", e);

        ApiResponse<Void> response = ApiResponse.error(500, localizedMessage);
        return ResponseEntity.status(500).body(response);
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
}
