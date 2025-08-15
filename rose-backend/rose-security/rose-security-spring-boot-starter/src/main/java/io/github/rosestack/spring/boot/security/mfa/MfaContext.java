package io.github.rosestack.spring.boot.security.mfa;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MFA上下文信息
 * <p>
 * 封装MFA认证过程中的上下文信息，包括请求信息、用户信息等。
 * 为MFA提供商提供必要的上下文数据以进行安全决策。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MfaContext {

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 客户端IP地址 */
    private String clientIp;

    /** 用户代理信息 */
    private String userAgent;

    /** 会话ID */
    private String sessionId;

    /** 请求ID（用于追踪） */
    private String requestId;

    /** 设备指纹 */
    private String deviceFingerprint;

    /** 地理位置信息 */
    private String location;

    /** 是否为可信设备 */
    private boolean trustedDevice;

    /** 认证级别 */
    private String authLevel;

    /** 扩展属性 */
    private Map<String, Object> attributes;

    /**
     * 获取扩展属性值
     *
     * @param key 属性键
     * @param <T> 属性值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return attributes != null ? (T) attributes.get(key) : null;
    }

    /**
     * 设置扩展属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new java.util.HashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * 创建简单的MFA上下文
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return MFA上下文
     */
    public static MfaContext simple(String userId, String username) {
        return MfaContext.builder().userId(userId).username(username).build();
    }
}
