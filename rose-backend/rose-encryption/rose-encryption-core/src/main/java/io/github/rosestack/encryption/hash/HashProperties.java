package io.github.rosestack.encryption.hash;

import lombok.Data;

/**
 * 哈希配置
 */
@Data
public class HashProperties {
    /**
     * 是否启用哈希功能
     */
    private boolean enabled = true;

    /**
     * 全局盐值（生产环境应该从外部配置获取）
     */
    private String globalSalt = "rose-mybatis-global-salt-2024";

    /**
     * 默认哈希算法
     */
    private String algorithm = "HMAC_SHA256";

    /**
     * HMAC 密钥（生产环境应该从外部配置获取）
     */
    private String hmacKey = "rose-mybatis-hmac-key-2024";
}
