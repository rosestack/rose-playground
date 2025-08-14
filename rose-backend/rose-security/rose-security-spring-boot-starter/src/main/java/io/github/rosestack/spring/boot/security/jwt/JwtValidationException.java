package io.github.rosestack.spring.boot.security.jwt;

/**
 * 一般校验失败异常（签名不通过、格式错误、标准声明不合规等）
 */
public class JwtValidationException extends RuntimeException {
    public JwtValidationException() { super(); }
    public JwtValidationException(String message) { super(message); }
    public JwtValidationException(String message, Throwable cause) { super(message, cause); }
}

