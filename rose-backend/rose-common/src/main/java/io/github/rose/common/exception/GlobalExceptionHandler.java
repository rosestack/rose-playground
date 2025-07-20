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
 * 捕获并处理应用程序中的各种异常，将它们转换为统一的响应格式。支持国际化错误消息，
 * 并根据异常类型返回适当的HTTP状态码。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>统一异常处理和响应格式</li>
 *   <li>支持国际化错误消息</li>
 *   <li>根据异常类型返回适当的HTTP状态码</li>
 *   <li>详细的日志记录</li>
 * </ul>
 *
 * @see io.github.rose.core.model.Result
 * @see io.github.rose.core.exception.BusinessException
 * @see ExceptionMessageResolver
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * <p>
     * 捕获业务异常并转换为统一的错误响应，记录警告级别日志，并解析国际化错误消息。
     *
     * @param e       业务异常实例
     * @param request HTTP请求对象
     * @return 包含错误信息的响应实体，HTTP状态码为400
     * @see BusinessException
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
     * <p>
     * 捕获限流异常并转换为统一的错误响应，记录警告级别日志，并解析国际化错误消息。
     * 返回429 Too Many Requests状态码。
     *
     * @param e 限流异常实例
     * @param request HTTP请求对象
     * @return 包含错误信息的响应实体，HTTP状态码为429
     * @see RateLimitException
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<Void>> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", e.getMessage(), e);

        // Resolve internationalized error message for rate limiting
        String message = ExceptionMessageResolver.resolveMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

    /**
     * 处理参数验证异常
     * <p>
     * 捕获表单和方法参数验证失败的异常，记录警告级别日志，并将所有字段错误格式化为统一的错误消息。
     * 支持处理MethodArgumentNotValidException和BindException两种类型的验证异常。
     *
     * @param e 验证异常实例
     * @param request HTTP请求对象
     * @return 包含格式化错误信息的响应实体，HTTP状态码为400
     * @see MethodArgumentNotValidException
     * @see BindException
     * @see #formatFieldError(FieldError)
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
     * 处理非法参数异常
     * <p>
     * 捕获非法参数异常并转换为统一的错误响应，记录警告级别日志，并解析国际化错误消息。
     *
     * @param e 非法参数异常实例
     * @param request HTTP请求对象
     * @return 包含错误信息的响应实体，HTTP状态码为400
     * @see IllegalArgumentException
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal argument exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveMessage(
                "illegal.argument"
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理空指针异常
     * <p>
     * 捕获空指针异常并转换为统一的错误响应，记录错误级别日志，并解析国际化错误消息。
     *
     * @param e 空指针异常实例
     * @param request HTTP请求对象
     * @return 包含错误信息的响应实体，HTTP状态码为500
     * @see NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("Null pointer exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveMessage(
                "null.pointer.error",
                "Null pointer error occurred"
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理系统异常
     * <p>
     * 捕获一般系统异常并转换为统一的错误响应，记录错误级别日志，并解析国际化错误消息。
     * 作为最后的异常处理手段，捕获所有未被其他处理器捕获的异常。
     *
     * @param e 系统异常实例
     * @param request HTTP请求对象
     * @return 包含错误信息的响应实体，HTTP状态码为500
     * @see Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleSystemException(Exception e, HttpServletRequest request) {
        log.error("System exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveMessage(
                "system.error",
                "Internal server error"
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理运行时异常
     * <p>
     * 捕获运行时异常并转换为统一的错误响应，记录错误级别日志，并解析国际化错误消息。
     * 处理所有未被特定处理器捕获的运行时异常。
     *
     * @param e 运行时异常实例
     * @param request HTTP请求对象
     * @return 包含错误信息的响应实体，HTTP状态码为500
     * @see RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        if (e instanceof BusinessException) {
            return handleBusinessException((BusinessException) e, request);
        }

        String message = ExceptionMessageResolver.resolveMessage(
                "runtime.error",
                "Runtime error occurred: " + e.getMessage()
        );

        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 格式化字段错误信息
     * <p>
     * 将验证错误对象转换为可读的错误消息字符串，包含字段名称和具体的错误信息。
     * 用于处理表单验证和请求参数验证的错误提示。
     *
     * @param fieldError 字段错误对象
     * @return 格式化后的错误消息字符串
     */
    private String formatFieldError(FieldError fieldError) {
        Locale locale = LocaleContextHolder.getLocale();

        String message = ExceptionMessageResolver.resolveMessage(
                fieldError.getCode(),
                fieldError.getDefaultMessage(),
                locale,
                fieldError.getArguments()
        );

        return fieldError.getField() + ": " + message;
    }
}
