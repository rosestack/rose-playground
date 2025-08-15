package io.github.rosestack.spring.boot.security.mfa.exception;

/**
 * MFA验证失败异常
 * <p>
 * 当MFA验证失败时抛出，包括验证码错误、验证码过期、重试次数超限等情况。
 * 提供详细的失败原因和建议的处理方式。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
public class MfaVerificationFailedException extends MfaException {

    /** 失败类型 */
    public enum FailureType {
        /** 验证码无效 */
        INVALID_CODE,
        /** 验证码已过期 */
        EXPIRED_CODE,
        /** 验证码已使用 */
        CODE_ALREADY_USED,
        /** 重试次数超限 */
        TOO_MANY_ATTEMPTS,
        /** 账号被锁定 */
        ACCOUNT_LOCKED,
        /** MFA未设置 */
        MFA_NOT_SETUP,
        /** 提供商不可用 */
        PROVIDER_UNAVAILABLE,
        /** 其他错误 */
        OTHER
    }

    /** 失败类型 */
    private final FailureType failureType;

    /** 剩余重试次数 */
    private final Integer remainingAttempts;

    /**
     * 构造MFA验证失败异常
     *
     * @param failureType 失败类型
     * @param message 错误消息
     */
    public MfaVerificationFailedException(FailureType failureType, String message) {
        super("MFA_VERIFICATION_FAILED", message);
        this.failureType = failureType;
        this.remainingAttempts = null;
    }

    /**
     * 构造MFA验证失败异常
     *
     * @param failureType 失败类型
     * @param message 错误消息
     * @param userId 用户ID
     */
    public MfaVerificationFailedException(FailureType failureType, String message, String userId) {
        super("MFA_VERIFICATION_FAILED", message, userId);
        this.failureType = failureType;
        this.remainingAttempts = null;
    }

    /**
     * 构造MFA验证失败异常
     *
     * @param failureType 失败类型
     * @param message 错误消息
     * @param userId 用户ID
     * @param remainingAttempts 剩余重试次数
     */
    public MfaVerificationFailedException(
            FailureType failureType, String message, String userId, Integer remainingAttempts) {
        super("MFA_VERIFICATION_FAILED", message, userId);
        this.failureType = failureType;
        this.remainingAttempts = remainingAttempts;
    }

    /**
     * 构造MFA验证失败异常
     *
     * @param failureType 失败类型
     * @param message 错误消息
     * @param cause 原始异常
     */
    public MfaVerificationFailedException(FailureType failureType, String message, Throwable cause) {
        super("MFA_VERIFICATION_FAILED", message, cause);
        this.failureType = failureType;
        this.remainingAttempts = null;
    }

    /**
     * 获取失败类型
     *
     * @return 失败类型
     */
    public FailureType getFailureType() {
        return failureType;
    }

    /**
     * 获取剩余重试次数
     *
     * @return 剩余重试次数，如果未设置返回null
     */
    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    /**
     * 创建验证码无效异常
     *
     * @param userId 用户ID
     * @param remainingAttempts 剩余重试次数
     * @return 异常实例
     */
    public static MfaVerificationFailedException invalidCode(String userId, Integer remainingAttempts) {
        return new MfaVerificationFailedException(FailureType.INVALID_CODE, "提供的MFA验证码无效", userId, remainingAttempts);
    }

    /**
     * 创建验证码过期异常
     *
     * @param userId 用户ID
     * @return 异常实例
     */
    public static MfaVerificationFailedException expiredCode(String userId) {
        return new MfaVerificationFailedException(FailureType.EXPIRED_CODE, "MFA验证码已过期，请重新获取", userId);
    }

    /**
     * 创建重试次数超限异常
     *
     * @param userId 用户ID
     * @return 异常实例
     */
    public static MfaVerificationFailedException tooManyAttempts(String userId) {
        return new MfaVerificationFailedException(FailureType.TOO_MANY_ATTEMPTS, "MFA验证失败次数过多，账号已被临时锁定", userId);
    }

    /**
     * 创建MFA未设置异常
     *
     * @param userId 用户ID
     * @return 异常实例
     */
    public static MfaVerificationFailedException mfaNotSetup(String userId) {
        return new MfaVerificationFailedException(FailureType.MFA_NOT_SETUP, "用户尚未设置MFA，请先完成MFA设置", userId);
    }
}
