package io.github.rosestack.spring.boot.security.jwt.exception;

/**
 * 在校验 JWT 时发现 token 过期抛出
 */
public class JwtTokenExpiredException extends JwtException {
    public JwtTokenExpiredException() {
        super("JWT token 已过期");
    }

    public JwtTokenExpiredException(String message) {
        super(message);
    }

    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
