package io.github.rosestack.spring.boot.security.mfa.exception;

import java.util.List;

/**
 * MFA必需异常
 * <p>
 * 当系统要求进行MFA验证但用户尚未完成验证时抛出。
 * 包含可用的MFA提供商信息和设置指引。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
public class MfaRequiredException extends MfaException {

    /** MFA状态 */
    public enum MfaStatus {
        /** 需要设置MFA */
        SETUP_REQUIRED,
        /** 需要验证MFA */
        VERIFICATION_REQUIRED,
        /** MFA已禁用 */
        MFA_DISABLED
    }

    /** MFA状态 */
    private final MfaStatus mfaStatus;

    /** 可用的MFA提供商 */
    private final List<String> availableProviders;

    /** 设置URL（如果需要设置） */
    private final String setupUrl;

    /**
     * 构造MFA必需异常
     *
     * @param mfaStatus MFA状态
     * @param message 错误消息
     * @param userId 用户ID
     */
    public MfaRequiredException(MfaStatus mfaStatus, String message, String userId) {
        super("MFA_REQUIRED", message, userId);
        this.mfaStatus = mfaStatus;
        this.availableProviders = null;
        this.setupUrl = null;
    }

    /**
     * 构造MFA必需异常
     *
     * @param mfaStatus MFA状态
     * @param message 错误消息
     * @param userId 用户ID
     * @param availableProviders 可用的MFA提供商
     */
    public MfaRequiredException(MfaStatus mfaStatus, String message, String userId, List<String> availableProviders) {
        super("MFA_REQUIRED", message, userId);
        this.mfaStatus = mfaStatus;
        this.availableProviders = availableProviders;
        this.setupUrl = null;
    }

    /**
     * 构造MFA必需异常
     *
     * @param mfaStatus MFA状态
     * @param message 错误消息
     * @param userId 用户ID
     * @param availableProviders 可用的MFA提供商
     * @param setupUrl 设置URL
     */
    public MfaRequiredException(
            MfaStatus mfaStatus, String message, String userId, List<String> availableProviders, String setupUrl) {
        super("MFA_REQUIRED", message, userId);
        this.mfaStatus = mfaStatus;
        this.availableProviders = availableProviders;
        this.setupUrl = setupUrl;
    }

    /**
     * 获取MFA状态
     *
     * @return MFA状态
     */
    public MfaStatus getMfaStatus() {
        return mfaStatus;
    }

    /**
     * 获取可用的MFA提供商
     *
     * @return 可用的MFA提供商列表
     */
    public List<String> getAvailableProviders() {
        return availableProviders;
    }

    /**
     * 获取设置URL
     *
     * @return 设置URL
     */
    public String getSetupUrl() {
        return setupUrl;
    }

    /**
     * 创建需要MFA设置异常
     *
     * @param userId 用户ID
     * @param availableProviders 可用的MFA提供商
     * @return 异常实例
     */
    public static MfaRequiredException setupRequired(String userId, List<String> availableProviders) {
        return new MfaRequiredException(MfaStatus.SETUP_REQUIRED, "此操作需要设置多因子认证，请先完成MFA设置", userId, availableProviders);
    }

    /**
     * 创建需要MFA验证异常
     *
     * @param userId 用户ID
     * @param availableProviders 可用的MFA提供商
     * @return 异常实例
     */
    public static MfaRequiredException verificationRequired(String userId, List<String> availableProviders) {
        return new MfaRequiredException(
                MfaStatus.VERIFICATION_REQUIRED, "此操作需要多因子认证验证，请提供MFA验证码", userId, availableProviders);
    }

    /**
     * 创建MFA已禁用异常
     *
     * @param userId 用户ID
     * @return 异常实例
     */
    public static MfaRequiredException mfaDisabled(String userId) {
        return new MfaRequiredException(MfaStatus.MFA_DISABLED, "多因子认证功能已禁用", userId);
    }
}
