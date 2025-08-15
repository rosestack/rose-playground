package io.github.rosestack.spring.boot.security.account;

import static org.junit.jupiter.api.Assertions.*;

import io.github.rosestack.spring.boot.security.account.impl.DefaultPasswordPolicyService;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码策略测试
 */
class PasswordPolicyTest {

    private PasswordPolicyService passwordPolicyService;
    private PasswordEncoder passwordEncoder;
    private RoseSecurityProperties.Account.Password passwordConfig;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordConfig = new RoseSecurityProperties.Account.Password();
        passwordPolicyService = new DefaultPasswordPolicyService(passwordConfig, passwordEncoder);
    }

    @Test
    void testPasswordValidation_Success() {
        // 测试符合策略的密码
        PasswordValidationResult result = passwordPolicyService.validatePassword("testuser", "Password123!");
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertTrue(result.getErrorMessages().isEmpty());
    }

    @Test
    void testPasswordValidation_TooShort() {
        // 测试密码过短
        PasswordValidationResult result = passwordPolicyService.validatePassword("testuser", "P1!");
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrorMessages().get(0).contains("密码长度不能少于"));
    }

    @Test
    void testPasswordValidation_MissingUppercase() {
        // 测试缺少大写字母
        PasswordValidationResult result = passwordPolicyService.validatePassword("testuser", "password123!");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessages().stream().anyMatch(msg -> msg.contains("大写字母")));
    }

    @Test
    void testPasswordValidation_MissingDigit() {
        // 测试缺少数字
        PasswordValidationResult result = passwordPolicyService.validatePassword("testuser", "Password!");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessages().stream().anyMatch(msg -> msg.contains("数字")));
    }

    @Test
    void testPasswordHistory() {
        String username = "testuser";
        String password1 = "Password123!";
        String password2 = "NewPassword456!";

        // 第一次密码验证应该成功
        PasswordValidationResult result1 = passwordPolicyService.validatePassword(username, password1);
        assertTrue(result1.isValid());

        // 记录密码历史
        String encodedPassword1 = passwordEncoder.encode(password1);
        passwordPolicyService.recordPasswordHistory(username, encodedPassword1);

        // 尝试使用相同密码应该失败
        PasswordValidationResult result2 = passwordPolicyService.validatePassword(username, password1);
        assertFalse(result2.isValid());
        assertTrue(result2.getErrorMessages().stream().anyMatch(msg -> msg.contains("最近")));

        // 使用新密码应该成功
        PasswordValidationResult result3 = passwordPolicyService.validatePassword(username, password2);
        assertTrue(result3.isValid());
    }

    @Test
    void testPasswordExpiration() {
        String username = "testuser";

        // 初始状态密码应该过期（没有记录）
        assertTrue(passwordPolicyService.isPasswordExpired(username));
        assertEquals(-1, passwordPolicyService.getPasswordExpirationDays(username));

        // 重置过期时间
        passwordPolicyService.resetPasswordExpiration(username);

        // 现在密码不应该过期
        assertFalse(passwordPolicyService.isPasswordExpired(username));
        assertTrue(passwordPolicyService.getPasswordExpirationDays(username) > 0);
    }
}
