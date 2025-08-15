package io.github.rosestack.spring.boot.security.account.impl;

import io.github.rosestack.spring.boot.security.account.PasswordExpiration;
import io.github.rosestack.spring.boot.security.account.PasswordHistory;
import io.github.rosestack.spring.boot.security.account.PasswordPolicyService;
import io.github.rosestack.spring.boot.security.account.PasswordValidationResult;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 默认密码策略服务实现
 *
 * <p>基于内存存储的密码策略服务，包括：
 * <ul>
 *   <li>密码复杂度验证</li>
 *   <li>密码历史记录管理</li>
 *   <li>密码过期检查</li>
 * </ul>
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultPasswordPolicyService implements PasswordPolicyService {

    private final RoseSecurityProperties.Account.Password passwordConfig;
    private final PasswordEncoder passwordEncoder;

    /** 密码历史记录存储：用户名 -> 密码历史列表 */
    private final Map<String, List<PasswordHistory>> passwordHistoryStore = new ConcurrentHashMap<>();

    /** 密码过期信息存储：用户名 -> 过期信息 */
    private final Map<String, PasswordExpiration> passwordExpirationStore = new ConcurrentHashMap<>();

    @Override
    public PasswordValidationResult validatePassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            return PasswordValidationResult.failure("用户名不能为空");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return PasswordValidationResult.failure("密码不能为空");
        }

        List<String> errors = new ArrayList<>();

        // 1. 检查密码长度
        if (newPassword.length() < passwordConfig.getMinLength()) {
            errors.add("密码长度不能少于 " + passwordConfig.getMinLength() + " 位");
        }

        // 2. 检查大写字母
        if (passwordConfig.isRequireUppercase()
                && !Pattern.compile("[A-Z]").matcher(newPassword).find()) {
            errors.add("密码必须包含至少一个大写字母");
        }

        // 3. 检查小写字母
        if (passwordConfig.isRequireLowercase()
                && !Pattern.compile("[a-z]").matcher(newPassword).find()) {
            errors.add("密码必须包含至少一个小写字母");
        }

        // 4. 检查数字
        if (passwordConfig.isRequireDigit()
                && !Pattern.compile("[0-9]").matcher(newPassword).find()) {
            errors.add("密码必须包含至少一个数字");
        }

        // 5. 检查特殊字符
        if (passwordConfig.isRequireSpecialChar()
                && !Pattern.compile("[^a-zA-Z0-9]").matcher(newPassword).find()) {
            errors.add("密码必须包含至少一个特殊字符");
        }

        // 6. 检查密码历史
        if (isPasswordInHistory(username, newPassword)) {
            errors.add("新密码不能与最近 " + passwordConfig.getHistory() + " 次使用的密码相同");
        }

        if (errors.isEmpty()) {
            return PasswordValidationResult.success();
        } else {
            return PasswordValidationResult.failure(errors);
        }
    }

    @Override
    public void recordPasswordHistory(String username, String encodedPassword) {
        if (username == null || encodedPassword == null) {
            return;
        }

        List<PasswordHistory> histories = passwordHistoryStore.computeIfAbsent(username, k -> new ArrayList<>());

        // 添加新的密码历史记录
        histories.add(PasswordHistory.of(username, encodedPassword));

        // 保持历史记录数量限制
        int maxHistory = passwordConfig.getHistory();
        if (histories.size() > maxHistory) {
            // 移除最老的记录，保留最新的记录
            histories.subList(0, histories.size() - maxHistory).clear();
        }

        // 重置密码过期时间
        resetPasswordExpiration(username);

        log.debug("记录用户 {} 的密码历史，当前历史记录数量: {}", username, histories.size());
    }

    @Override
    public boolean isPasswordExpired(String username) {
        if (passwordConfig.getExpireDays() <= 0) {
            return false; // 密码永不过期
        }

        PasswordExpiration expiration = passwordExpirationStore.get(username);
        if (expiration == null) {
            // 如果没有记录，认为密码已过期，需要重新设置
            return true;
        }

        return expiration.isExpired();
    }

    @Override
    public long getPasswordExpirationDays(String username) {
        if (passwordConfig.getExpireDays() <= 0) {
            return Long.MAX_VALUE; // 密码永不过期
        }

        PasswordExpiration expiration = passwordExpirationStore.get(username);
        if (expiration == null) {
            return -1; // 已过期
        }

        return expiration.getDaysUntilExpiration();
    }

    @Override
    public void resetPasswordExpiration(String username) {
        if (passwordConfig.getExpireDays() > 0) {
            PasswordExpiration expiration = PasswordExpiration.of(username, passwordConfig.getExpireDays());
            passwordExpirationStore.put(username, expiration);
            log.debug("重置用户 {} 的密码过期时间，过期日期: {}", username, expiration.getExpirationTime());
        }
    }

    /**
     * 检查新密码是否在历史记录中
     *
     * @param username 用户名
     * @param newPassword 新密码（明文）
     * @return true如果在历史记录中
     */
    private boolean isPasswordInHistory(String username, String newPassword) {
        List<PasswordHistory> histories = passwordHistoryStore.get(username);
        if (histories == null || histories.isEmpty()) {
            return false;
        }

        // 检查新密码是否与历史密码匹配
        for (PasswordHistory history : histories) {
            if (passwordEncoder.matches(newPassword, history.getEncodedPassword())) {
                return true;
            }
        }

        return false;
    }
}
