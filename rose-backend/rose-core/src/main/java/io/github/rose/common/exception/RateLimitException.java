
package io.github.rose.common.exception;

public class RateLimitException extends BusinessException {
    public RateLimitException(String code, Object... args) {
        super(code, args);
    }
}
