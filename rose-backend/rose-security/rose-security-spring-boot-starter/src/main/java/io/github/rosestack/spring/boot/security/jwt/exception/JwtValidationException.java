package io.github.rosestack.spring.boot.security.jwt.exception;

/**
 * 一般校验失败异常（签名不通过、格式错误、标准声明不合规等）
 */
public class JwtValidationException extends JwtException {
    public JwtValidationException() {
        super("JWT 验证失败");
    }

    public JwtValidationException(String message) {
        super(message);
    }

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
