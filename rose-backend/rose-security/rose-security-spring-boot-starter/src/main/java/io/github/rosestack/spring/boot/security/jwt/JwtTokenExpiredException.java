package io.github.rosestack.spring.boot.security.jwt;

/**
 * 在校验 JWT 时发现 token 过期抛出
 */
public class JwtTokenExpiredException extends RuntimeException {
    public JwtTokenExpiredException() { super(); }
    public JwtTokenExpiredException(String message) { super(message); }
    public JwtTokenExpiredException(String message, Throwable cause) { super(message, cause); }
}

