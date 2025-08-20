package io.github.rosestack.spring.boot.encryption.config;

import io.github.rosestack.encryption.hash.HashProperties;
import io.github.rosestack.encryption.rotation.KeyRotationProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.regex.Pattern;

/**
 * Rose加密配置属性
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "rose.encryption")
public class EncryptionProperties {

    /**
     * 是否启用加密功能
     */
    private boolean enabled = true;

    /**
     * 加密密钥（生产环境应该从外部配置或密钥管理系统获取）
     * 要求：至少16个字符，建议32个字符以上
     */
    private String secretKey;

    /**
     * 加密失败时是否抛出异常
     * true: 抛出异常（推荐，便于发现问题）
     * false: 返回原始值（可能导致数据泄露）
     */
    private boolean failOnError = true;

    /**
     * 密钥轮换配置
     */
    private KeyRotationProperties keyRotation = new KeyRotationProperties();

    /**
     * 哈希配置
     */
    private HashProperties hash = new HashProperties();

    /**
     * 弱密钥模式列表
     */
    private static final Pattern[] WEAK_KEY_PATTERNS = {
        Pattern.compile("^(password|secret|key|admin|test|demo).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^(123|abc|111|000).*"),
        Pattern.compile("^(.{1,8})\\1+$"), // 重复字符
        Pattern.compile("^[a-z]+$|^[A-Z]+$|^[0-9]+$") // 单一字符类型
    };

    /**
     * 配置验证
     * 在自动配置类中手动调用
     */
    public void validateConfiguration() {
        if (!enabled) {
            log.info("Rose加密功能已禁用");
            return;
        }

        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("启用加密功能时必须配置密钥");
        }

        // 密钥强度检查
        validateKeyStrength(secretKey);

        // 生产环境安全检查
        if (isProductionEnvironment()) {
            validateProductionSecurity();
        }

        log.info("Rose加密配置验证通过");
    }

    /**
     * 验证密钥强度
     */
    private void validateKeyStrength(String key) {
        if (key.length() < 32) {
            log.warn("密钥长度小于32字符，建议使用更长的密钥以提高安全性");
        }

        // 检查弱密钥模式
        for (Pattern pattern : WEAK_KEY_PATTERNS) {
            if (pattern.matcher(key).matches()) {
                log.warn("检测到弱密钥模式，建议使用更强的密钥");
                break;
            }
        }

        // 检查字符多样性
        boolean hasLower = key.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = key.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = key.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = key.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        int diversity = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        if (diversity < 3) {
            log.warn("密钥字符多样性不足，建议包含大小写字母、数字和特殊字符");
        }
    }

    /**
     * 生产环境安全检查
     */
    private void validateProductionSecurity() {
        // 检查是否使用默认密钥
        if ("0123456789abcdeffedcba9876543210".equals(secretKey)) {
            throw new IllegalArgumentException("生产环境不能使用默认密钥");
        }

        // 检查是否通过环境变量配置
        String envKey = System.getenv("ROSE_ENCRYPTION_SECRET_KEY");
        if (envKey == null || envKey.trim().isEmpty()) {
            log.warn("建议通过环境变量 ROSE_ENCRYPTION_SECRET_KEY 配置密钥");
        }

        if (!failOnError) {
            log.warn("生产环境建议启用 failOnError 以便及时发现加密问题");
        }
    }

    /**
     * 判断是否为生产环境
     */
    private boolean isProductionEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null) {
            profile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        return profile != null && (profile.contains("prod") || profile.contains("production"));
    }
}
