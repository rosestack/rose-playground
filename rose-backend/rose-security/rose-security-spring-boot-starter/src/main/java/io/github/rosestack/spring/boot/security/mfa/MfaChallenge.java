package io.github.rosestack.spring.boot.security.mfa;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * MFA挑战信息
 * <p>
 * 封装MFA认证过程中的挑战信息，如二维码、密钥、验证步骤等。
 * 不同类型的MFA提供商可以通过扩展数据字段来传递特定信息。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@Builder
public class MfaChallenge {

    /** 挑战ID */
    private String challengeId;

    /** MFA提供商类型 */
    private String providerType;

    /** 用户ID */
    private String userId;

    /** 挑战类型（setup、verify、backup等） */
    private String challengeType;

    /** 主要挑战数据（如密钥、二维码URL等） */
    private String challengeData;

    /** 显示文本（给用户的提示信息） */
    private String displayText;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 是否已使用 */
    private boolean used;

    /** 扩展属性 */
    private Map<String, Object> properties;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /**
     * 获取扩展属性值
     *
     * @param key 属性键
     * @param <T> 属性值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return properties != null ? (T) properties.get(key) : null;
    }

    /**
     * 设置扩展属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new java.util.HashMap<>();
        }
        properties.put(key, value);
    }

    /**
     * 检查挑战是否已过期
     *
     * @return 如果已过期返回true
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
