package io.github.rose.common.exception;

import io.github.rose.core.exception.BusinessException;
import io.github.rose.core.exception.RateLimitException;
import io.github.rose.core.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 处理Controller层的异常，提供统一的异常响应格式和国际化支持
 * 配合优化后的BusinessException使用
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveBusinessExceptionMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理限流异常
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<Void>> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", e.getMessage(), e);

        // 使用ExceptionMessageResolver处理国际化
        String message = ExceptionMessageResolver.resolveBusinessExceptionMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidationException(Exception e, HttpServletRequest request) {
        log.warn("Validation exception occurred: {}", e.getMessage(), e);

        String message;
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .collect(Collectors.joining("; "));
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .collect(Collectors.joining("; "));
        } else {
            message = "Validation failed";
        }

        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal argument exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveI18nMessage(
                "illegal.argument",
                e.getMessage(),
                e.getMessage()
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("Null pointer exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveI18nMessage(
                "null.pointer.error",
                "Null pointer error occurred"
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleSystemException(Exception e, HttpServletRequest request) {
        log.error("System exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveI18nMessage(
                "system.error",
                "Internal server error"
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        // 如果是已知的业务异常类型，使用特定处理
        if (e instanceof BusinessException) {
            return handleBusinessException((BusinessException) e, request);
        }

        // 其他运行时异常
        String message = ExceptionMessageResolver.resolveI18nMessage(
                "runtime.error",
                "Runtime error occurred: " + e.getMessage(),
                e.getMessage()
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 格式化字段错误信息
     */
    private String formatFieldError(FieldError fieldError) {
        Locale locale = LocaleContextHolder.getLocale();

        // 尝试国际化字段错误消息
        String message = ExceptionMessageResolver.resolveMessage(
                fieldError.getCode(),
                fieldError.getArguments(),
                fieldError.getDefaultMessage(),
                locale
        );

        return fieldError.getField() + ": " + message;
    }
}
