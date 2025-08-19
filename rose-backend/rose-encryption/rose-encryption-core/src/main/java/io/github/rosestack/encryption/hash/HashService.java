package io.github.rosestack.encryption.hash;

import io.github.rosestack.encryption.annotation.EncryptField;
import io.github.rosestack.encryption.enums.HashType;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 哈希服务
 *
 * <p>提供安全的哈希计算功能，支持多种哈希算法和盐值策略。 用于生成加密字段的哈希值，支持精确查询。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class HashService {
    private final HashProperties properties;
    private final Boolean failOnError;

    /**
     * 生成哈希值
     *
     * @param plainText 明文
     * @param hashType  哈希算法类型
     * @return 哈希值（十六进制字符串）
     */
    public String generateHash(String plainText, HashType hashType) {
        if (StringUtils.isBlank(plainText)) {
            return plainText;
        }

        try {
            switch (hashType) {
                case SHA256:
                    return sha256WithSalt(plainText);
                case SHA512:
                    return sha512WithSalt(plainText);
                case HMAC_SHA256:
                    return hmacSha256(plainText);
                case HMAC_SHA512:
                    return hmacSha512(plainText);
                default:
                    throw new IllegalArgumentException("不支持的哈希类型: " + hashType);
            }
        } catch (Exception e) {
            log.error("哈希计算失败: {}", e.getMessage(), e);
            if (failOnError) {
                throw new RuntimeException("哈希计算失败", e);
            }
            return plainText;
        }
    }

    /**
     * SHA-256 加盐哈希
     */
    private String sha256WithSalt(String plainText) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String salt = properties.getGlobalSalt();
        String saltedText = plainText + salt;
        byte[] hash = digest.digest(saltedText.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * SHA-512 加盐哈希
     */
    private String sha512WithSalt(String plainText) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        String salt = properties.getGlobalSalt();
        String saltedText = plainText + salt;
        byte[] hash = digest.digest(saltedText.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * HMAC-SHA256 哈希
     */
    private String hmacSha256(String plainText) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        String key = properties.getHmacKey();
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * HMAC-SHA512 哈希
     */
    private String hmacSha512(String plainText) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        String key = properties.getHmacKey();
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 验证哈希值（防时序攻击）
     *
     * @param plainText  明文
     * @param storedHash 存储的哈希值
     * @param hashType   哈希算法类型
     * @return 是否匹配
     */
    public boolean verifyHash(String plainText, String storedHash, HashType hashType) {
        if (StringUtils.isBlank(plainText) || StringUtils.isBlank(storedHash)) {
            return false;
        }

        String computedHash = generateHash(plainText, hashType);

        // 使用常量时间比较，防止时序攻击
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8), storedHash.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用默认算法生成哈希值
     *
     * @param plainText 明文
     * @return 哈希值
     */
    public String generateHashWithDefault(String plainText) {
        HashType defaultHashType = getDefaultHashType();
        return generateHash(plainText, defaultHashType);
    }

    /**
     * 根据配置获取默认哈希类型
     *
     * @return 默认哈希类型
     */
    public HashType getDefaultHashType() {
        String algorithm = properties.getAlgorithm().toUpperCase();

        return HashType.valueOf(algorithm);
    }

    /**
     * 生成哈希字段名
     *
     * @param originalFieldName 原字段名
     * @param customHashField   自定义哈希字段名
     * @return 哈希字段名
     */
    public String generateHashFieldName(String originalFieldName, String customHashField) {
        if (StringUtils.isNotBlank(customHashField)) {
            return customHashField;
        }
        return originalFieldName + "_hash";
    }

    /**
     * 根据实体类字段注解生成哈希值
     *
     * <p>自动从字段的 @EncryptField 注解中获取哈希算法类型
     *
     * @param plainText   明文
     * @param entityClass 实体类
     * @param fieldName   字段名
     * @return 哈希值
     */
    public String generateHashByField(String plainText, Class<?> entityClass, String fieldName) {
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            EncryptField encryptField = field.getAnnotation(EncryptField.class);

            if (encryptField == null || !encryptField.searchable()) {
                throw new IllegalArgumentException("字段 " + fieldName + " 没有 @EncryptField 注解或不支持查询");
            }

            HashType hashType = encryptField.hashType();
            return generateHash(plainText, hashType);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("字段 " + fieldName + " 不存在于类 " + entityClass.getSimpleName(), e);
        }
    }

    /**
     * 常量时间字符串比较（防时序攻击）
     *
     * <p>使用常量时间比较算法，避免因为字符串长度或内容差异导致的时序攻击
     *
     * @param a 字符串 A
     * @param b 字符串 B
     * @return 是否相等
     */
    public boolean constantTimeEquals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();

        // 使用固定长度比较，防止长度泄露
        int length = Math.max(aBytes.length, bBytes.length);
        int result = aBytes.length ^ bBytes.length;

        for (int i = 0; i < length; i++) {
            byte aByte = i < aBytes.length ? aBytes[i] : 0;
            byte bByte = i < bBytes.length ? bBytes[i] : 0;
            result |= aByte ^ bByte;
        }

        return result == 0;
    }
}
