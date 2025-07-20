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
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e       业务异常
     * @param request HTTP 请求
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理限流异常
     *
     * @param e       限流异常
     * @param request HTTP 请求
     * @return 错误响应，状态码 429
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<Void>> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

    /**
     * 处理参数验证异常
     *
     * @param e       验证异常
     * @param request HTTP 请求
     * @return 错误响应，包含验证失败的字段信息
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

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal argument exception occurred: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage("illegal.argument");
        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("Null pointer exception occurred: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage("null.pointer.error", "Null pointer error occurred");
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleSystemException(Exception e, HttpServletRequest request) {
        log.error("System exception occurred: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage("system.error", "Internal server error");
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    private String formatFieldError(FieldError fieldError) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = ExceptionMessageResolver.resolveMessage(fieldError.getCode(), fieldError.getDefaultMessage(), locale, fieldError.getArguments());
        return fieldError.getField() + ": " + message;
    }
}
