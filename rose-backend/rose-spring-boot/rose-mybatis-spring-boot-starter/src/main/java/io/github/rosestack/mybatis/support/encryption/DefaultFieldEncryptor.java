package io.github.rosestack.mybatis.support.encryption;

import com.antherd.smcrypto.sm2.Sm2;
import com.antherd.smcrypto.sm4.Sm4;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优化的字段加密器实现
 * <p>
 * 相比默认实现，提供以下优化：
 * 1. Cipher 实例缓存，避免重复创建
 * 2. 批量加密支持，提升批量操作性能
 * 3. 线程安全的缓存机制
 * 4. 更好的错误处理和监控
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
     * Cipher 实例缓存 - 使用 ThreadLocal 确保线程安全
     */
    private static final ThreadLocal<Map<String, Cipher>> ENCRYPT_CIPHER_CACHE =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    private static final ThreadLocal<Map<String, Cipher>> DECRYPT_CIPHER_CACHE =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 性能统计
     */
    private static final Map<String, Long> PERFORMANCE_STATS = new ConcurrentHashMap<>();

    @Override
    public String encrypt(String plainText, EncryptType encryptType) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }

        if (!properties.getEncryption().isEnabled()) {
            log.debug("字段加密已禁用，返回原始值");
            return plainText;
        }

        long startTime = System.nanoTime();
        try {
            String result = doEncrypt(plainText, encryptType);
            recordPerformance("encrypt_" + encryptType.name(), startTime);
            return result;
        } catch (Exception e) {
            log.error("字段加密失败: {}", e.getMessage(), e);
            if (properties.getEncryption().isFailOnError()) {
                throw new RuntimeException("字段加密失败", e);
            }
            return plainText;
        }
    }

    @Override
    public String decrypt(String cipherText, EncryptType encryptType) {
        if (!StringUtils.hasText(cipherText)) {
            return cipherText;
        }

        if (!properties.getEncryption().isEnabled()) {
            log.debug("字段加密已禁用，返回原始值");
            return cipherText;
        }

        long startTime = System.nanoTime();
        try {
            String result = doDecrypt(cipherText, encryptType);
            recordPerformance("decrypt_" + encryptType.name(), startTime);
            return result;
        } catch (Exception e) {
            log.error("字段解密失败: {}", e.getMessage(), e);
            if (properties.getEncryption().isFailOnError()) {
                throw new RuntimeException("字段解密失败", e);
            }
            return cipherText;
        }
    }

    /**
     * 执行加密
     */
    private String doEncrypt(String plainText, EncryptType encryptType) throws Exception {
        String key = getEncryptionKey();
        if (encryptType == EncryptType.SM4) {
            return Sm4.encrypt(plainText, key);
        } else if (encryptType == EncryptType.SM2) {
            return Sm2.doEncrypt(plainText, key);
        }
        Cipher cipher = getEncryptCipher(encryptType);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 执行解密
     */
    private String doDecrypt(String cipherText, EncryptType encryptType) throws Exception {
        String key = getEncryptionKey();
        if (encryptType == EncryptType.SM4) {
            return Sm4.decrypt(cipherText, key);
        } else if (encryptType == EncryptType.SM2) {
            return Sm2.doDecrypt(cipherText, key);
        }

        Cipher cipher = getDecryptCipher(encryptType);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 获取加密 Cipher 实例（带缓存）
     */
    private Cipher getEncryptCipher(EncryptType encryptType) throws Exception {
        String cacheKey = "encrypt_" + encryptType.name();
        return ENCRYPT_CIPHER_CACHE.get().computeIfAbsent(cacheKey, k -> {
            try {
                return createEncryptCipher(encryptType);
            } catch (Exception e) {
                throw new RuntimeException("创建加密 Cipher 失败", e);
            }
        });
    }

    /**
     * 获取解密 Cipher 实例（带缓存）
     */
    private Cipher getDecryptCipher(EncryptType encryptType) throws Exception {
        String cacheKey = "decrypt_" + encryptType.name();
        return DECRYPT_CIPHER_CACHE.get().computeIfAbsent(cacheKey, k -> {
            try {
                return createDecryptCipher(encryptType);
            } catch (Exception e) {
                throw new RuntimeException("创建解密 Cipher 失败", e);
            }
        });
    }

    /**
     * 创建加密 Cipher
     */
    private Cipher createEncryptCipher(EncryptType encryptType) throws Exception {
        String key = getEncryptionKey();
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), encryptType.name());
        Cipher cipher = Cipher.getInstance(encryptType.name());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher;
    }

    /**
     * 创建解密 Cipher
     */
    private Cipher createDecryptCipher(EncryptType algorithm) throws Exception {
        String key = getEncryptionKey();
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm.name());
        Cipher cipher = Cipher.getInstance(algorithm.name());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher;
    }

    /**
     * 获取加密密钥
     */
    private String getEncryptionKey() {
        String secretKey = properties.getEncryption().getSecretKey();
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("未配置加密密钥");
        }

        return secretKey;
    }

    /**
     * 记录性能统计
     */
    private void recordPerformance(String operation, long startTime) {
        long duration = System.nanoTime() - startTime;
        PERFORMANCE_STATS.merge(operation, duration, Long::sum);
    }

    /**
     * 获取性能统计
     */
    public static Map<String, Long> getPerformanceStats() {
        return new HashMap<>(PERFORMANCE_STATS);
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        ENCRYPT_CIPHER_CACHE.remove();
        DECRYPT_CIPHER_CACHE.remove();
        PERFORMANCE_STATS.clear();
        log.info("已清空加密器缓存");
    }
}
