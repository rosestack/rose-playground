package io.github.rosestack.audit.util;

import io.github.rosestack.audit.properties.AuditProperties;
import io.github.rosestack.mybatis.support.encryption.EncryptType;
import io.github.rosestack.mybatis.support.encryption.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 审计加密工具类
 * <p>
 * 提供审计日志数据的加密解密功能，基于 mybatis 模块的通用加密工具。
 * 支持多种加密算法，线程安全，高性能缓存。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public final class AuditEncryptionUtils {

    private AuditEncryptionUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 加密敏感数据
     *
     * @param plainText  明文
     * @param properties 审计配置
     * @return 加密后的密文，如果加密失败且配置允许则返回原文
     */
    public static String encryptSensitiveData(String plainText, AuditProperties properties) {
        if (!properties.getEncryption().isEnabled()) {
            log.debug("审计加密已禁用，返回原始值");
            return plainText;
        }

        EncryptType encryptType = getEncryptType(properties.getEncryption().getAlgorithm());
        String secretKey = properties.getEncryption().getSecretKey();
        boolean failOnError = properties.getEncryption().isFailOnError();

        return EncryptionUtils.encrypt(plainText, encryptType, secretKey, failOnError);
    }

    /**
     * 解密敏感数据
     *
     * @param cipherText 密文
     * @param properties 审计配置
     * @return 解密后的明文，如果解密失败且配置允许则返回原文
     */
    public static String decryptSensitiveData(String cipherText, AuditProperties properties) {
        if (!properties.getEncryption().isEnabled()) {
            log.debug("审计加密已禁用，返回原始值");
            return cipherText;
        }

        EncryptType encryptType = getEncryptType(properties.getEncryption().getAlgorithm());
        String secretKey = properties.getEncryption().getSecretKey();
        boolean failOnError = properties.getEncryption().isFailOnError();

        return EncryptionUtils.decrypt(cipherText, encryptType, secretKey, failOnError);
    }

    /**
     * 根据算法名称获取加密类型
     */
    private static EncryptType getEncryptType(String algorithm) {
        if (!StringUtils.hasText(algorithm)) {
            return EncryptType.AES; // 默认使用 AES
        }

        try {
            return EncryptType.valueOf(algorithm.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不支持的加密算法: {}, 使用默认的 AES", algorithm);
            return EncryptType.AES;
        }
    }
}