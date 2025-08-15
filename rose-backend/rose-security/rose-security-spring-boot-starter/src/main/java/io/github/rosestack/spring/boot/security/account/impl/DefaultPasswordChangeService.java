package io.github.rosestack.spring.boot.security.account.impl;

import io.github.rosestack.spring.boot.security.account.PasswordChangeResult;
import io.github.rosestack.spring.boot.security.account.PasswordChangeService;
import io.github.rosestack.spring.boot.security.account.PasswordPolicyService;
import io.github.rosestack.spring.boot.security.account.PasswordValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 默认密码变更服务实现
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultPasswordChangeService implements PasswordChangeService {

    private final PasswordPolicyService passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public PasswordChangeResult changePassword(String username, String oldPassword, String newPassword) {
        try {
            // 1. 验证用户存在
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 2. 验证旧密码
            if (!passwordEncoder.matches(oldPassword, userDetails.getPassword())) {
                return PasswordChangeResult.failure("当前密码不正确");
            }

            // 3. 验证新密码策略
            PasswordValidationResult validation = passwordPolicyService.validatePassword(username, newPassword);
            if (!validation.isValid()) {
                String errorMessage = String.join("; ", validation.getErrorMessages());
                return PasswordChangeResult.failure(errorMessage);
            }

            // 4. 加密新密码
            String encodedNewPassword = passwordEncoder.encode(newPassword);

            // 5. 记录密码历史
            passwordPolicyService.recordPasswordHistory(username, encodedNewPassword);

            log.info("用户 {} 成功修改密码", username);
            return PasswordChangeResult.success(encodedNewPassword);

        } catch (UsernameNotFoundException e) {
            log.warn("修改密码失败，用户不存在: {}", username);
            return PasswordChangeResult.failure("用户不存在");
        } catch (Exception e) {
            log.error("修改密码时发生异常: {}", e.getMessage(), e);
            return PasswordChangeResult.failure("密码修改失败，请稍后重试");
        }
    }

    @Override
    public PasswordChangeResult resetPassword(String username, String newPassword) {
        try {
            // 1. 验证用户存在
            userDetailsService.loadUserByUsername(username);

            // 2. 验证新密码策略
            PasswordValidationResult validation = passwordPolicyService.validatePassword(username, newPassword);
            if (!validation.isValid()) {
                String errorMessage = String.join("; ", validation.getErrorMessages());
                return PasswordChangeResult.failure(errorMessage);
            }

            // 3. 加密新密码
            String encodedNewPassword = passwordEncoder.encode(newPassword);

            // 4. 记录密码历史
            passwordPolicyService.recordPasswordHistory(username, encodedNewPassword);

            log.info("管理员重置用户 {} 的密码", username);
            return PasswordChangeResult.success(encodedNewPassword);

        } catch (UsernameNotFoundException e) {
            log.warn("重置密码失败，用户不存在: {}", username);
            return PasswordChangeResult.failure("用户不存在");
        } catch (Exception e) {
            log.error("重置密码时发生异常: {}", e.getMessage(), e);
            return PasswordChangeResult.failure("密码重置失败，请稍后重试");
        }
    }
}
