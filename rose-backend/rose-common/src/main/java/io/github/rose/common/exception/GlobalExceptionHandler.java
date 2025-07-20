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
 * Global exception handler for centralized exception processing in web applications.
 *
 * This class provides comprehensive exception handling for all controller layer exceptions,
 * ensuring consistent error response formats and internationalization support throughout
 * the application. It integrates with the ExceptionMessageResolver for localized error
 * messages and follows REST API best practices for error responses.
 *
 * <h3>Exception Handling Strategy:</h3>
 * <ul>
 *   <li><strong>Business Exceptions:</strong> Application-specific business logic violations</li>
 *   <li><strong>Validation Exceptions:</strong> Bean validation and method argument validation failures</li>
 *   <li><strong>Rate Limiting:</strong> Request rate limit exceeded scenarios</li>
 *   <li><strong>System Exceptions:</strong> Unexpected runtime errors and system failures</li>
 *   <li><strong>Security Exceptions:</strong> Authentication and authorization failures</li>
 * </ul>
 *
 * <h3>Response Format:</h3>
 * All exception handlers return a consistent Result&lt;Void&gt; format wrapped in ResponseEntity
 * with appropriate HTTP status codes. The Result object contains:
 * <ul>
 *   <li>Success flag (always false for exceptions)</li>
 *   <li>Localized error message</li>
 *   <li>Error code (when applicable)</li>
 *   <li>Additional context information</li>
 * </ul>
 *
 * <h3>Internationalization Support:</h3>
 * Error messages are resolved through ExceptionMessageResolver, which provides:
 * <ul>
 *   <li>Locale-specific error messages</li>
 *   <li>Fallback to default messages</li>
 *   <li>Parameter substitution for dynamic content</li>
 *   <li>Integration with Spring's MessageSource</li>
 * </ul>
 *
 * <h3>Logging Strategy:</h3>
 * <ul>
 *   <li><strong>Business Exceptions:</strong> WARN level (expected application behavior)</li>
 *   <li><strong>Validation Exceptions:</strong> WARN level (client input issues)</li>
 *   <li><strong>System Exceptions:</strong> ERROR level (unexpected failures)</li>
 *   <li><strong>Security Exceptions:</strong> WARN level (potential security issues)</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see ExceptionMessageResolver
 * @see Result
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business logic exceptions thrown by application services.
     *
     * Business exceptions represent expected error conditions in the application's
     * business logic, such as validation failures, resource not found, or business
     * rule violations. These are typically recoverable errors that should be
     * communicated clearly to the client.
     *
     * <p><strong>Response Characteristics:</strong>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 400 Bad Request</li>
     *   <li><strong>Logging Level:</strong> WARN (expected application behavior)</li>
     *   <li><strong>Message Resolution:</strong> Through ExceptionMessageResolver for i18n</li>
     *   <li><strong>Response Format:</strong> Standardized Result object</li>
     * </ul>
     *
     * <p><strong>Exception Processing:</strong>
     * <ol>
     *   <li>Log the exception at WARN level with context</li>
     *   <li>Resolve localized error message using ExceptionMessageResolver</li>
     *   <li>Create failure Result with resolved message</li>
     *   <li>Return ResponseEntity with 400 status</li>
     * </ol>
     *
     * @param e The BusinessException that was thrown
     * @param request The HTTP request that caused the exception (for context)
     * @return ResponseEntity with failure Result and 400 Bad Request status
     *
     * @see BusinessException
     * @see ExceptionMessageResolver#resolveMessage(BusinessException)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);

        String message = ExceptionMessageResolver.resolveMessage(e);
        Result<Void> result = Result.failure(message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Handles rate limiting exceptions when request limits are exceeded.
     *
     * Rate limit exceptions occur when clients exceed the configured request
     * rate limits for API endpoints. This is a protective mechanism to prevent
     * abuse and ensure fair resource usage across all clients.
     *
     * <p><strong>Response Characteristics:</strong>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 429 Too Many Requests</li>
     *   <li><strong>Logging Level:</strong> WARN (potential abuse or misconfiguration)</li>
     *   <li><strong>Message Resolution:</strong> Through ExceptionMessageResolver for i18n</li>
     *   <li><strong>Response Format:</strong> Standardized Result object</li>
     * </ul>
     *
     * <p><strong>Rate Limiting Context:</strong>
     * Rate limiting helps protect the application from:
     * <ul>
     *   <li>Denial of Service (DoS) attacks</li>
     *   <li>Resource exhaustion</li>
     *   <li>Unfair resource consumption</li>
     *   <li>Accidental client loops</li>
     * </ul>
     *
     * @param e The RateLimitException that was thrown
     * @param request The HTTP request that exceeded rate limits (for context)
     * @return ResponseEntity with failure Result and 429 Too Many Requests status
     *
     * @see RateLimitException
     * @see ExceptionMessageResolver#resolveMessage(BusinessException)
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
     * Handles Bean Validation exceptions for request parameter and body validation.
     *
     * @param e The validation exception that occurred
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with failure Result and 400 Bad Request status
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
     * Handles illegal argument exceptions for invalid method parameters.
     *
     * @param e The IllegalArgumentException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with failure Result and 400 Bad Request status
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
     * Handles null pointer exceptions indicating programming errors.
     *
     * @param e The NullPointerException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with failure Result and 500 Internal Server Error status
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
     * Handles general system exceptions as a catch-all for unexpected errors.
     *
     * @param e The Exception that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with failure Result and 500 Internal Server Error status
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
     * Handles runtime exceptions with special handling for known business exceptions.
     *
     * @param e The RuntimeException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with failure Result and appropriate HTTP status
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
     * Formats field validation error messages with internationalization support.
     *
     * @param fieldError The field error to format
     * @return Formatted error message with field name and localized message
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
