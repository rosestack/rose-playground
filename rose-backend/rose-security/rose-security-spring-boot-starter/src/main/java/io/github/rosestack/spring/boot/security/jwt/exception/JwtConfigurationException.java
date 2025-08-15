package io.github.rosestack.spring.boot.security.jwt.exception;

/**
 * JWT 配置异常
 *
 * <p>当 JWT 配置不正确时抛出此异常
 */
public class JwtConfigurationException extends JwtException {

    public JwtConfigurationException(String message) {
        super(message);
    }

    public JwtConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
