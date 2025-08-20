package io.github.rosestack.crypto.exception;

/**
 * 加密相关异常
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class CryptoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }
}
