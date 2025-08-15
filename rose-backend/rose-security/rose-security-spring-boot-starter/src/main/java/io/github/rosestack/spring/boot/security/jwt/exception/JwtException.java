package io.github.rosestack.spring.boot.security.jwt.exception;

/**
 * JWT 基础异常类
 *
 * <p>所有 JWT 相关异常的基类，提供统一的异常处理
 */
public class JwtException extends RuntimeException {

    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
