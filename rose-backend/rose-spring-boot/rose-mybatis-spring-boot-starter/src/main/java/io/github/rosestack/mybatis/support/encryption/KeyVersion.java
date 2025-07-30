package io.github.rosestack.mybatis.support.encryption;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密钥版本信息
 * <p>
 * 用于管理密钥的版本化，支持密钥轮换功能
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyVersion {

    /**
     * 密钥版本号（唯一标识）
     */
    private String version;

    /**
     * 加密密钥
     */
    private String secretKey;

    /**
     * HMAC 密钥
     */
    private String hmacKey;

    /**
     * 密钥创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 密钥激活时间
     */
    private LocalDateTime activeTime;

    /**
     * 密钥过期时间（可选）
     */
    private LocalDateTime expireTime;

    /**
     * 是否为当前活跃版本
     */
    private boolean active;

    /**
     * 是否已废弃（废弃后不能用于加密，只能解密）
     */
    private boolean deprecated;

    /**
     * 密钥描述
     */
    private String description;

    /**
     * 检查密钥是否可用于加密
     */
    public boolean canEncrypt() {
        return active && !deprecated && 
               (expireTime == null || LocalDateTime.now().isBefore(expireTime));
    }

    /**
     * 检查密钥是否可用于解密
     */
    public boolean canDecrypt() {
        return !deprecated && 
               (expireTime == null || LocalDateTime.now().isBefore(expireTime.plusDays(30))); // 解密宽限期30天
    }

    /**
     * 创建默认密钥版本
     */
    public static KeyVersion createDefault(String secretKey, String hmacKey) {
        return KeyVersion.builder()
                .version("v1")
                .secretKey(secretKey)
                .hmacKey(hmacKey)
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .description("Default key version")
                .build();
    }
}
