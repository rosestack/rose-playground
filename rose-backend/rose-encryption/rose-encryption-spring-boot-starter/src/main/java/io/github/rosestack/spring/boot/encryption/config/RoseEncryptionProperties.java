package io.github.rosestack.spring.boot.encryption.config;

import io.github.rosestack.encryption.hash.HashProperties;
import io.github.rosestack.encryption.rotation.KeyRotationProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rose.encryption")
public class RoseEncryptionProperties {
    private boolean enabled = true;

    /**
     * 加密密钥（生产环境应该从外部配置或密钥管理系统获取）
     */
    private String secretKey = "0123456789abcdeffedcba9876543210";

    /**
     * 加密失败时是否抛出异常
     */
    private boolean failOnError = true;

    /**
     * 密钥轮换配置
     */
    private KeyRotationProperties keyRotationProperties = new KeyRotationProperties();

    /**
     * 哈希配置
     */
    private HashProperties hashProperties = new HashProperties();
}
