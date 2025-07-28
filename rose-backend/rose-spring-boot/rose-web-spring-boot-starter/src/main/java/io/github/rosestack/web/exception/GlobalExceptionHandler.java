package io.github.rosestack.web.exception;

import io.github.rosestack.core.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * 全局异常处理器
 * <p>
 * 统一处理业务异常、参数校验异常、系统异常，支持国际化
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex, Locale locale) {
        String message = getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        log.warn("业务异常: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.businessError(message));
    }

    /**
     * 处理限流异常
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(RateLimitException ex, Locale locale) {
        String message = getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);
        log.warn("限流异常: {}", message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(429, message));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex, Locale locale) {
        String message = "参数校验失败";
        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else if (ex instanceof BindException be) {
            message = be.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else if (ex instanceof ConstraintViolationException cve) {
            message = cve.getConstraintViolations().iterator().next().getMessage();
        }
        log.warn("参数校验异常: {}", message);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.validationError(message));
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, Locale locale) {
        log.error("系统异常", ex);
        String message = getMessage("system.error", null, locale);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }

    private String getMessage(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return key;
        }
    }
} 