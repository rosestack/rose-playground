package io.github.rosestack.encryption;

import com.antherd.smcrypto.sm2.Sm2;
import com.antherd.smcrypto.sm4.Sm4;
import io.github.rosestack.encryption.enums.EncryptType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 通用加密工具类
 *
 * <p>提供统一的加密解密功能，支持多种加密算法。 被 FieldEncryptor 和审计模块共同使用，避免代码重复。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public final class EncryptionUtils {

    /**
     * Cipher 实例缓存 - 线程安全
     */
    private static final ConcurrentMap<String, Cipher> ENCRYPT_CIPHER_CACHE =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Cipher> DECRYPT_CIPHER_CACHE =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Long> PERFORMANCE_STATS = new ConcurrentHashMap<>();
    private static final String DEFAULT_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private EncryptionUtils() {
    }

    /**
     * 加密数据
     *
     * @param plainText   明文
     * @param encryptType 加密类型
     * @param secretKey   密钥
     * @return 加密后的密文，如果加密失败且不抛出异常则返回原文
     */
    public static String encrypt(String plainText, EncryptType encryptType, String secretKey) {
        if (StringUtils.isBlank(plainText)) {
            return plainText;
        }

        if (StringUtils.isBlank(secretKey)) {
            log.warn("加密密钥为空，返回原始值");
            return plainText;
        }

        long startTime = System.nanoTime();
        try {
            String result = doEncrypt(plainText, encryptType, secretKey);
            recordPerformance("encrypt_" + encryptType.name(), startTime);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * 解密数据
     *
     * @param cipherText  密文
     * @param encryptType 加密类型
     * @param secretKey   密钥
     * @return 解密后的明文，如果解密失败且不抛出异常则返回原文
     */
    public static String decrypt(String cipherText, EncryptType encryptType, String secretKey) {
        if (StringUtils.isBlank(cipherText)) {
            return cipherText;
        }

        if (StringUtils.isBlank(secretKey)) {
            log.warn("解密密钥为空，返回原始值");
            return cipherText;
        }

        long startTime = System.nanoTime();
        try {
            String result = doDecrypt(cipherText, encryptType, secretKey);
            recordPerformance("decrypt_" + encryptType.name(), startTime);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("数据解密失败", e);
        }
    }

    /**
     * 批量加密
     *
     * @param plainTexts  明文数组
     * @param encryptType 加密类型
     * @param secretKey   密钥
     * @return 加密后的密文数组
     */
    public static String[] encryptBatch(
            String[] plainTexts, EncryptType encryptType, String secretKey) {
        if (plainTexts == null) {
            return null;
        }

        String[] results = new String[plainTexts.length];
        for (int i = 0; i < plainTexts.length; i++) {
            results[i] = encrypt(plainTexts[i], encryptType, secretKey);
        }
        return results;
    }

    /**
     * 批量解密
     *
     * @param cipherTexts 密文数组
     * @param encryptType 加密类型
     * @param secretKey   密钥
     * @return 解密后的明文数组
     */
    public static String[] decryptBatch(
            String[] cipherTexts, EncryptType encryptType, String secretKey) {
        if (cipherTexts == null) {
            return null;
        }

        String[] results = new String[cipherTexts.length];
        for (int i = 0; i < cipherTexts.length; i++) {
            results[i] = decrypt(cipherTexts[i], encryptType, secretKey);
        }
        return results;
    }

    /**
     * 生成哈希值
     *
     * @param data 原始数据
     * @return SHA-256 哈希值
     */
    public static String generateHash(String data) {
        if (StringUtils.isBlank(data)) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("生成哈希值失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成哈希值失败", e);
        }
    }

    /**
     * 验证哈希值
     *
     * @param data 原始数据
     * @param hash 哈希值
     * @return 是否匹配
     */
    public static boolean verifyHash(String data, String hash) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(hash)) {
            return false;
        }

        try {
            String computedHash = generateHash(data);
            return hash.equals(computedHash);
        } catch (Exception e) {
            log.error("验证哈希值失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行加密
     */
    private static String doEncrypt(String plainText, EncryptType encryptType, String secretKey)
            throws Exception {
        if (encryptType == EncryptType.SM4) {
            return Sm4.encrypt(plainText, secretKey);
        } else if (encryptType == EncryptType.SM2) {
            return Sm2.doEncrypt(plainText, secretKey);
        }

        Cipher cipher = getEncryptCipher(encryptType, secretKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 执行解密
     */
    private static String doDecrypt(String cipherText, EncryptType encryptType, String secretKey)
            throws Exception {
        if (encryptType == EncryptType.SM4) {
            return Sm4.decrypt(cipherText, secretKey);
        } else if (encryptType == EncryptType.SM2) {
            return Sm2.doDecrypt(cipherText, secretKey);
        }

        Cipher cipher = getDecryptCipher(encryptType, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 获取加密 Cipher 实例（带缓存）
     */
    private static Cipher getEncryptCipher(EncryptType encryptType, String secretKey)
            throws Exception {
        String cacheKey = "encrypt_" + encryptType.name() + "_" + secretKey.hashCode();
        return ENCRYPT_CIPHER_CACHE.computeIfAbsent(
                cacheKey,
                k -> {
                    try {
                        return createEncryptCipher(encryptType, secretKey);
                    } catch (Exception e) {
                        throw new RuntimeException("创建加密 Cipher 失败", e);
                    }
                });
    }

    /**
     * 获取解密 Cipher 实例（带缓存）
     */
    private static Cipher getDecryptCipher(EncryptType encryptType, String secretKey)
            throws Exception {
        String cacheKey = "decrypt_" + encryptType.name() + "_" + secretKey.hashCode();
        return DECRYPT_CIPHER_CACHE.computeIfAbsent(
                cacheKey,
                k -> {
                    try {
                        return createDecryptCipher(encryptType, secretKey);
                    } catch (Exception e) {
                        throw new RuntimeException("创建解密 Cipher 失败", e);
                    }
                });
    }

    /**
     * 创建加密 Cipher
     */
    private static Cipher createEncryptCipher(EncryptType encryptType, String secretKey)
            throws Exception {
        String algorithm = encryptType == EncryptType.AES ? DEFAULT_TRANSFORMATION : encryptType.name();
        SecretKeySpec keySpec =
                new SecretKeySpec(adjustKeyLength(secretKey, encryptType), encryptType.name());
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher;
    }

    /**
     * 创建解密 Cipher
     */
    private static Cipher createDecryptCipher(EncryptType encryptType, String secretKey)
            throws Exception {
        String algorithm = encryptType == EncryptType.AES ? DEFAULT_TRANSFORMATION : encryptType.name();
        SecretKeySpec keySpec =
                new SecretKeySpec(adjustKeyLength(secretKey, encryptType), encryptType.name());
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher;
    }

    /**
     * 调整密钥长度
     */
    private static byte[] adjustKeyLength(String secretKey, EncryptType encryptType) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        int requiredLength = getRequiredKeyLength(encryptType);

        if (keyBytes.length == requiredLength) {
            return keyBytes;
        }

        byte[] adjustedKey = new byte[requiredLength];
        if (keyBytes.length > requiredLength) {
            System.arraycopy(keyBytes, 0, adjustedKey, 0, requiredLength);
        } else {
            System.arraycopy(keyBytes, 0, adjustedKey, 0, keyBytes.length);
            // 用0填充剩余部分
            for (int i = keyBytes.length; i < requiredLength; i++) {
                adjustedKey[i] = 0;
            }
        }
        return adjustedKey;
    }

    /**
     * 获取所需密钥长度
     */
    private static int getRequiredKeyLength(EncryptType encryptType) {
        switch (encryptType) {
            case AES:
                return 16; // 128位
            case DES:
                return 8; // 64位
            default:
                return 16;
        }
    }

    /**
     * 记录性能统计
     */
    private static void recordPerformance(String operation, long startTime) {
        long duration = System.nanoTime() - startTime;
        PERFORMANCE_STATS.merge(operation, duration, Long::sum);
    }

    /**
     * 获取性能统计
     */
    public static ConcurrentMap<String, Long> getPerformanceStats() {
        return new ConcurrentHashMap<>(PERFORMANCE_STATS);
    }

    /**
     * 获取缓存统计信息
     */
    public static String getCacheStats() {
        return String.format(
                "加密缓存: %d, 解密缓存: %d", ENCRYPT_CIPHER_CACHE.size(), DECRYPT_CIPHER_CACHE.size());
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        ENCRYPT_CIPHER_CACHE.clear();
        DECRYPT_CIPHER_CACHE.clear();
        PERFORMANCE_STATS.clear();
        log.info("已清空加密工具缓存");
    }
}
