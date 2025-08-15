package io.github.rosestack.spring.boot.security.mfa.exception;

/**
 * MFA基础异常类
 * <p>
 * 所有MFA相关异常的基类，提供统一的异常处理机制。
 * 包含错误代码和详细错误信息，便于问题排查和用户反馈。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
public class MfaException extends RuntimeException {

    /** 错误代码 */
    private final String errorCode;

    /** 用户ID */
    private final String userId;

    /**
     * 构造MFA异常
     *
     * @param message 错误消息
     */
    public MfaException(String message) {
        super(message);
        this.errorCode = "MFA_ERROR";
        this.userId = null;
    }

    /**
     * 构造MFA异常
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public MfaException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MFA_ERROR";
        this.userId = null;
    }

    /**
     * 构造MFA异常
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public MfaException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.userId = null;
    }

    /**
     * 构造MFA异常
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param cause 原始异常
     */
    public MfaException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userId = null;
    }

    /**
     * 构造MFA异常
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param userId 用户ID
     */
    public MfaException(String errorCode, String message, String userId) {
        super(message);
        this.errorCode = errorCode;
        this.userId = userId;
    }

    /**
     * 构造MFA异常
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param userId 用户ID
     * @param cause 原始异常
     */
    public MfaException(String errorCode, String message, String userId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userId = userId;
    }

    /**
     * 获取错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        String result = getClass().getSimpleName() + "[" + errorCode + "]: " + getMessage();
        if (userId != null) {
            result += " (userId: " + userId + ")";
        }
        return result;
    }
}
