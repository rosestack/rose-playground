package io.github.rosestack.mybatis.encryption;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 默认字段加密器实现
 * <p>
 * 提供基于 AES、DES、3DES 等算法的字段加密和解密功能。
 * 支持配置化的密钥管理和加密开关。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultFieldEncryptor implements FieldEncryptor {

    private final RoseMybatisProperties properties;

    /**
     * 默认 AES 密钥（生产环境应该从配置中读取）
     */
    private static final String DEFAULT_AES_KEY = "MySecretKey12345"; // 16字节

    @Override
    public String encrypt(String plainText, EncryptField.EncryptType encryptType) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }

        if (!properties.getEncryption().isEnabled()) {
            log.debug("字段加密已禁用，返回原始值");
            return plainText;
        }

        try {
            switch (encryptType) {
                case AES:
                    return encryptAES(plainText);
                case DES:
                    return encryptDES(plainText);
                case DES3:
                    return encrypt3DES(plainText);
                case SM4:
                    // SM4 需要额外的依赖，这里先返回原始值
                    log.warn("SM4 加密暂未实现，返回原始值");
                    return plainText;
                default:
                    log.warn("不支持的加密类型: {}，返回原始值", encryptType);
                    return plainText;
            }
        } catch (Exception e) {
            log.error("字段加密失败: {}", e.getMessage(), e);
            // 加密失败时根据配置决定是否抛出异常
            if (properties.getEncryption().isFailOnError()) {
                throw new RuntimeException("字段加密失败", e);
            }
            return plainText;
        }
    }

    @Override
    public String decrypt(String cipherText, EncryptField.EncryptType encryptType) {
        if (!StringUtils.hasText(cipherText)) {
            return cipherText;
        }

        if (!properties.getEncryption().isEnabled()) {
            log.debug("字段加密已禁用，返回原始值");
            return cipherText;
        }

        try {
            switch (encryptType) {
                case AES:
                    return decryptAES(cipherText);
                case DES:
                    return decryptDES(cipherText);
                case DES3:
                    return decrypt3DES(cipherText);
                case SM4:
                    // SM4 需要额外的依赖，这里先返回原始值
                    log.warn("SM4 解密暂未实现，返回原始值");
                    return cipherText;
                default:
                    log.warn("不支持的解密类型: {}，返回原始值", encryptType);
                    return cipherText;
            }
        } catch (Exception e) {
            log.error("字段解密失败: {}", e.getMessage(), e);
            // 解密失败时根据配置决定是否抛出异常
            if (properties.getEncryption().isFailOnError()) {
                throw new RuntimeException("字段解密失败", e);
            }
            return cipherText;
        }
    }

    @Override
    public boolean supports(EncryptField.EncryptType encryptType) {
        return encryptType == EncryptField.EncryptType.AES ||
               encryptType == EncryptField.EncryptType.DES ||
               encryptType == EncryptField.EncryptType.DES3;
    }

    /**
     * AES 加密
     */
    private String encryptAES(String plainText) throws Exception {
        String key = getEncryptionKey("AES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES 解密
     */
    private String decryptAES(String cipherText) throws Exception {
        String key = getEncryptionKey("AES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * DES 加密
     */
    private String encryptDES(String plainText) throws Exception {
        String key = getEncryptionKey("DES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * DES 解密
     */
    private String decryptDES(String cipherText) throws Exception {
        String key = getEncryptionKey("DES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 3DES 加密
     */
    private String encrypt3DES(String plainText) throws Exception {
        String key = getEncryptionKey("DESede");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DESede");
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 3DES 解密
     */
    private String decrypt3DES(String cipherText) throws Exception {
        String key = getEncryptionKey("DESede");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "DESede");
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 获取加密密钥
     */
    private String getEncryptionKey(String algorithm) {
        String configKey = properties.getEncryption().getSecretKey();
        if (StringUtils.hasText(configKey)) {
            return adjustKeyLength(configKey, algorithm);
        }

        // 使用默认密钥（生产环境不推荐）
        log.warn("未配置加密密钥，使用默认密钥（生产环境不推荐）");
        return adjustKeyLength(DEFAULT_AES_KEY, algorithm);
    }

    /**
     * 调整密钥长度以适应不同算法
     */
    private String adjustKeyLength(String key, String algorithm) {
        switch (algorithm) {
            case "DES":
                // DES 需要 8 字节密钥
                return adjustToLength(key, 8);
            case "DESede":
                // 3DES 需要 24 字节密钥
                return adjustToLength(key, 24);
            case "AES":
            default:
                // AES 需要 16 字节密钥
                return adjustToLength(key, 16);
        }
    }

    /**
     * 调整字符串到指定长度
     */
    private String adjustToLength(String input, int targetLength) {
        if (input.length() == targetLength) {
            return input;
        } else if (input.length() > targetLength) {
            return input.substring(0, targetLength);
        } else {
            // 如果长度不够，用 '0' 填充
            StringBuilder sb = new StringBuilder(input);
            while (sb.length() < targetLength) {
                sb.append('0');
            }
            return sb.toString();
        }
    }

    /**
     * 生成随机密钥（工具方法）
     */
    public static String generateKey(String algorithm, int keyLength) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(keyLength, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
