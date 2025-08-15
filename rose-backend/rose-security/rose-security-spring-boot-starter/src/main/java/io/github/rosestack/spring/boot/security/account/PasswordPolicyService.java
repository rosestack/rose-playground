package io.github.rosestack.spring.boot.security.account;

/**
 * 密码策略服务接口
 *
 * <p>提供密码复杂度验证、密码历史记录管理、密码过期检查等功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface PasswordPolicyService {

    /**
     * 验证密码是否符合策略要求
     *
     * @param username 用户名
     * @param newPassword 新密码
     * @return 验证结果
     */
    PasswordValidationResult validatePassword(String username, String newPassword);

    /**
     * 记录用户密码变更历史
     *
     * @param username 用户名
     * @param encodedPassword 加密后的密码
     */
    void recordPasswordHistory(String username, String encodedPassword);

    /**
     * 检查密码是否过期
     *
     * @param username 用户名
     * @return true如果密码已过期
     */
    boolean isPasswordExpired(String username);

    /**
     * 获取密码过期剩余天数
     *
     * @param username 用户名
     * @return 剩余天数，负数表示已过期
     */
    long getPasswordExpirationDays(String username);

    /**
     * 重置用户密码过期时间
     *
     * @param username 用户名
     */
    void resetPasswordExpiration(String username);
}
