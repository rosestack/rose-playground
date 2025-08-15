package io.github.rosestack.spring.boot.security.account;

/**
 * 密码变更服务接口
 *
 * <p>提供密码修改功能，集成密码策略验证
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface PasswordChangeService {

    /**
     * 修改用户密码
     *
     * @param username 用户名
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 修改结果
     */
    PasswordChangeResult changePassword(String username, String oldPassword, String newPassword);

    /**
     * 重置用户密码（管理员操作）
     *
     * @param username 用户名
     * @param newPassword 新密码（明文）
     * @return 重置结果
     */
    PasswordChangeResult resetPassword(String username, String newPassword);
}
