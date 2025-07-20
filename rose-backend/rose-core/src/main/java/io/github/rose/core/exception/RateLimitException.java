
package io.github.rose.core.exception;

import java.io.Serial;

/**
 * Rate limiting exception for handling request rate limit violations.
 *
 * This exception is thrown when a client exceeds the configured rate limits for API endpoints
 * or system resources. It extends BusinessException to inherit internationalization support
 * and consistent error handling patterns.
 *
 * <h3>Common Use Cases:</h3>
 * <ul>
 *   <li><strong>API Rate Limiting:</strong> When clients exceed API call limits</li>
 *   <li><strong>Resource Protection:</strong> When system resources are being overused</li>
 *   <li><strong>Abuse Prevention:</strong> When detecting potential abuse patterns</li>
 *   <li><strong>Fair Usage:</strong> When enforcing fair usage policies</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple rate limit violation
 * throw new RateLimitException("Rate limit exceeded: 100 requests per minute");
 *
 * // Internationalized rate limit message
 * throw new RateLimitException("rate.limit.exceeded", new Object[]{100, "minute"});
 *
 * // With fallback message
 * throw new RateLimitException("rate.limit.exceeded", "Rate limit exceeded", new Object[]{100});
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see BusinessException
 */
public class RateLimitException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a rate limit exception with a simple message (no internationalization).
     *
     * @param message The error message describing the rate limit violation
     */
    public RateLimitException(String message) {
        super(message);
    }

    /**
     * Creates a rate limit exception with a simple message and cause (no internationalization).
     *
     * @param message The error message describing the rate limit violation
     * @param cause The underlying cause of this exception
     */
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an internationalized rate limit exception with message code and arguments.
     *
     * @param messageCode The internationalization message code
     * @param messageArgs Arguments for message template substitution
     */
    public RateLimitException(String messageCode, Object[] messageArgs) {
        super(messageCode, messageArgs);
    }

    /**
     * Creates an internationalized rate limit exception with fallback message.
     *
     * @param messageCode The internationalization message code
     * @param defaultMessage The default error message used as fallback
     * @param messageArgs Arguments for message template substitution
     */
    public RateLimitException(String messageCode, String defaultMessage, Object[] messageArgs) {
        super(messageCode, defaultMessage, messageArgs);
    }

}
