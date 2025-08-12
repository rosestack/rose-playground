package io.github.rosestack.spring.boot.redis.exception;

/**
 * 分布式锁异常基类
 *
 * <p>所有分布式锁相关的异常都继承自此类。
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class LockException extends RuntimeException {

    public LockException() {
        super();
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}
