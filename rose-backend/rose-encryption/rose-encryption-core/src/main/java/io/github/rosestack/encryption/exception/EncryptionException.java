package io.github.rosestack.encryption.exception;

/**
 * 加密相关异常
 *
 * @author Rose Team
 * @since 1.0.0
 */
public class EncryptionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(Throwable cause) {
        super(cause);
    }
}
