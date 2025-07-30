package io.github.rosestack.redis.exception;

/**
 * 锁超时异常
 * <p>
 * 当等待获取锁超时时抛出此异常。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class LockTimeoutException extends LockException {

    public LockTimeoutException() {
        super();
    }

    public LockTimeoutException(String message) {
        super(message);
    }

    public LockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockTimeoutException(Throwable cause) {
        super(cause);
    }
}