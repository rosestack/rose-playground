package io.github.rosestack.spring.boot.security.mfa;

/**
 * 多因子认证提供商接口
 * <p>
 * 定义了MFA认证的核心操作，支持不同类型的MFA实现（TOTP、SMS、邮件等）。
 * 实现此接口可以扩展自定义的MFA认证方式。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
public interface MfaProvider {

    /**
     * 获取MFA提供商类型
     *
     * @return MFA类型标识
     */
    String getType();

    /**
     * 检查用户是否已设置MFA
     *
     * @param userId 用户ID
     * @return 如果已设置返回true，否则返回false
     */
    boolean isSetup(String userId);

    /**
     * 初始化用户的MFA设置
     *
     * @param userId 用户ID
     * @param context MFA上下文
     * @return MFA挑战信息，包含设置所需的信息（如二维码、密钥等）
     * @throws io.github.rosestack.spring.boot.security.mfa.exception.MfaException MFA异常
     */
    MfaChallenge initSetup(String userId, MfaContext context);

    /**
     * 完成MFA设置
     *
     * @param userId 用户ID
     * @param challenge 初始化时返回的挑战信息
     * @param verificationCode 用户提供的验证码
     * @param context MFA上下文
     * @return 设置结果
     * @throws io.github.rosestack.spring.boot.security.mfa.exception.MfaException MFA异常
     */
    MfaResult completeSetup(String userId, MfaChallenge challenge, String verificationCode, MfaContext context);

    /**
     * 验证MFA码
     *
     * @param userId 用户ID
     * @param verificationCode 用户提供的验证码
     * @param context MFA上下文
     * @return 验证结果
     * @throws io.github.rosestack.spring.boot.security.mfa.exception.MfaException MFA异常
     */
    MfaResult verify(String userId, String verificationCode, MfaContext context);

    /**
     * 移除用户的MFA设置
     *
     * @param userId 用户ID
     * @param context MFA上下文
     * @return 操作结果
     * @throws io.github.rosestack.spring.boot.security.mfa.exception.MfaException MFA异常
     */
    MfaResult removeSetup(String userId, MfaContext context);

    /**
     * 生成备用恢复码
     *
     * @param userId 用户ID
     * @param context MFA上下文
     * @return 包含备用恢复码的挑战信息
     * @throws io.github.rosestack.spring.boot.security.mfa.exception.MfaException MFA异常
     */
    default MfaChallenge generateBackupCodes(String userId, MfaContext context) {
        throw new UnsupportedOperationException("此MFA提供商不支持备用恢复码");
    }

    /**
     * 验证备用恢复码
     *
     * @param userId 用户ID
     * @param backupCode 备用恢复码
     * @param context MFA上下文
     * @return 验证结果
     * @throws io.github.rosestack.spring.boot.security.mfa.exception.MfaException MFA异常
     */
    default MfaResult verifyBackupCode(String userId, String backupCode, MfaContext context) {
        throw new UnsupportedOperationException("此MFA提供商不支持备用恢复码验证");
    }
}
