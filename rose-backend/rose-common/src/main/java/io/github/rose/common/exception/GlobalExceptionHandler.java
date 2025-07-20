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



@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<Void>> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", e.getMessage(), e);
        String message = ExceptionMessageResolver.resolveMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        if (e instanceof BusinessException) {
            return handleBusinessException((BusinessException) e, request);
        }

        String message = ExceptionMessageResolver.resolveMessage("runtime.error", "Runtime error occurred: " + e.getMessage());
        Result<Void> result = Result.failure(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    private String formatFieldError(FieldError fieldError) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = ExceptionMessageResolver.resolveMessage(fieldError.getCode(), fieldError.getDefaultMessage(), locale, fieldError.getArguments());
        return fieldError.getField() + ": " + message;
    }
}
