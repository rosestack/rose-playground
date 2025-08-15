package io.github.rosestack.spring.boot.security.mfa.totp;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TOTP密钥信息
 * <p>
 * 封装TOTP密钥及其相关信息，包括密钥本身、创建时间、使用状态等。
 * 提供密钥的完整生命周期管理信息。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotpSecret {

    /** 用户ID */
    private String userId;

    /** Base32编码的密钥 */
    private String secret;

    /** 账户名称（通常是用户名或邮箱） */
    private String accountName;

    /** 发行者名称 */
    private String issuer;

    /** QR码数据URL */
    private String qrCodeUrl;

    /** 是否已验证（设置完成） */
    private boolean verified;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后验证时间 */
    private LocalDateTime lastVerifiedAt;

    /** 最后使用的时间窗口（防重放） */
    private Long lastUsedWindow;

    /** 验证失败次数 */
    private int failureCount;

    /** 最后失败时间 */
    private LocalDateTime lastFailureAt;

    /**
     * 检查密钥是否可用
     *
     * @return 如果可用返回true
     */
    public boolean isUsable() {
        return secret != null && !secret.trim().isEmpty();
    }

    /**
     * 检查密钥是否已过期（可根据需要实现）
     *
     * @return 如果已过期返回true
     */
    public boolean isExpired() {
        // 默认TOTP密钥不过期，可根据安全策略实现
        return false;
    }

    /**
     * 重置失败次数
     */
    public void resetFailureCount() {
        this.failureCount = 0;
        this.lastFailureAt = null;
    }

    /**
     * 增加失败次数
     */
    public void incrementFailureCount() {
        this.failureCount++;
        this.lastFailureAt = LocalDateTime.now();
    }

    /**
     * 更新最后验证信息
     *
     * @param timeWindow 使用的时间窗口
     */
    public void updateLastVerified(long timeWindow) {
        this.lastVerifiedAt = LocalDateTime.now();
        this.lastUsedWindow = timeWindow;
        this.resetFailureCount();
    }

    /**
     * 检查时间窗口是否已使用（防重放）
     *
     * @param timeWindow 时间窗口
     * @return 如果已使用返回true
     */
    public boolean isWindowUsed(long timeWindow) {
        return lastUsedWindow != null && lastUsedWindow.equals(timeWindow);
    }

    /**
     * 创建简单的TOTP密钥
     *
     * @param userId 用户ID
     * @param secret Base32编码的密钥
     * @param accountName 账户名称
     * @param issuer 发行者
     * @return TOTP密钥实例
     */
    public static TotpSecret create(String userId, String secret, String accountName, String issuer) {
        return TotpSecret.builder()
                .userId(userId)
                .secret(secret)
                .accountName(accountName)
                .issuer(issuer)
                .verified(false)
                .failureCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
