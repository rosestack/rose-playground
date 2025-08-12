package io.github.rosestack.spring.boot.redis.exception;

/**
 * 锁获取异常
 *
 * <p>当无法获取锁时抛出此异常。
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class LockAcquisitionException extends LockException {

    public LockAcquisitionException() {
        super();
    }

    public LockAcquisitionException(String message) {
        super(message);
    }

    public LockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockAcquisitionException(Throwable cause) {
        super(cause);
    }
}
