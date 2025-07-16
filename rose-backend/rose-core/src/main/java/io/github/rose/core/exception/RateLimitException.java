
package io.github.rose.core.exception;

public class RateLimitException extends BusinessException {
    public RateLimitException(String code, Object... args) {
        super(code, args);
    }
}
