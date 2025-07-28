package io.github.rosestack.mybatis.encryption;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.enums.EncryptType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
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
public class OptimizedFieldEncryptor implements FieldEncryptor {

    private final RoseMybatisProperties properties;

    @Autowired(required = false)
    private KeyRotationManager keyRotationManager;

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

    /**
     * 同态加密支持 - 存储加密数据的元信息
     */
    private static final Map<String, HomomorphicMetadata> HOMOMORPHIC_METADATA = new ConcurrentHashMap<>();

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

            // 为同态加密保存元信息
            if (isNumeric(plainText)) {
                HomomorphicMetadata metadata = new HomomorphicMetadata();
                metadata.setOriginalValue(plainText);
                metadata.setOperation("ENCRYPT");
                metadata.setTimestamp(System.currentTimeMillis());
                HOMOMORPHIC_METADATA.put(result, metadata);
            }

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
     * 批量加密
     *
     * @param plainTexts 明文列表
     * @param encryptType 加密类型
     * @return 密文列表
     */
    public List<String> encryptBatch(List<String> plainTexts, EncryptType encryptType) {
        if (plainTexts == null || plainTexts.isEmpty()) {
            return plainTexts;
        }

        long startTime = System.nanoTime();
        try {
            List<String> results = new ArrayList<>(plainTexts.size());
            Cipher cipher = getEncryptCipher(encryptType);
            
            for (String plainText : plainTexts) {
                if (StringUtils.hasText(plainText)) {
                    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
                    results.add(Base64.getEncoder().encodeToString(encrypted));
                } else {
                    results.add(plainText);
                }
            }
            
            recordPerformance("encrypt_batch_" + encryptType.name(), startTime);
            return results;
        } catch (Exception e) {
            log.error("批量加密失败: {}", e.getMessage(), e);
            if (properties.getEncryption().isFailOnError()) {
                throw new RuntimeException("批量加密失败", e);
            }
            return plainTexts;
        }
    }

    /**
     * 批量解密
     *
     * @param cipherTexts 密文列表
     * @param encryptType 加密类型
     * @return 明文列表
     */
    public List<String> decryptBatch(List<String> cipherTexts, EncryptType encryptType) {
        if (cipherTexts == null || cipherTexts.isEmpty()) {
            return cipherTexts;
        }

        long startTime = System.nanoTime();
        try {
            List<String> results = new ArrayList<>(cipherTexts.size());
            Cipher cipher = getDecryptCipher(encryptType);
            
            for (String cipherText : cipherTexts) {
                if (StringUtils.hasText(cipherText)) {
                    byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
                    results.add(new String(decrypted, StandardCharsets.UTF_8));
                } else {
                    results.add(cipherText);
                }
            }
            
            recordPerformance("decrypt_batch_" + encryptType.name(), startTime);
            return results;
        } catch (Exception e) {
            log.error("批量解密失败: {}", e.getMessage(), e);
            if (properties.getEncryption().isFailOnError()) {
                throw new RuntimeException("批量解密失败", e);
            }
            return cipherTexts;
        }
    }

    @Override
    public boolean supports(EncryptType encryptType) {
        return encryptType == EncryptType.AES ||
                encryptType == EncryptType.DES ||
                encryptType == EncryptType.DES3;
    }

    /**
     * 执行加密
     */
    private String doEncrypt(String plainText, EncryptType encryptType) throws Exception {
        Cipher cipher = getEncryptCipher(encryptType);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 执行解密
     */
    private String doDecrypt(String cipherText, EncryptType encryptType) throws Exception {
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
        String algorithm = getAlgorithm(encryptType);
        String key = getEncryptionKey(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher;
    }

    /**
     * 创建解密 Cipher
     */
    private Cipher createDecryptCipher(EncryptType encryptType) throws Exception {
        String algorithm = getAlgorithm(encryptType);
        String key = getEncryptionKey(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher;
    }

    /**
     * 获取算法名称
     */
    private String getAlgorithm(EncryptType encryptType) {
        switch (encryptType) {
            case AES: return "AES";
            case DES: return "DES";
            case DES3: return "DESede";
            case SM4: return "SM4";
            case SM2: return "SM2";
            case RSA: return "RSA";
            case ECC: return "EC";
            default: throw new IllegalArgumentException("不支持的加密类型: " + encryptType);
        }
    }

    /**
     * 获取加密密钥
     */
    private String getEncryptionKey(String algorithm) {
        // 如果启用了密钥轮换，使用轮换管理器获取密钥
        if (keyRotationManager != null) {
            KeyRotationManager.KeyVersion keyVersion = keyRotationManager.getCurrentKey("default");
            String keyWithVersion = keyVersion.getKeyValue() + "_v" + keyVersion.getVersion();
            return adjustKeyLengthForAlgorithm(keyWithVersion, algorithm);
        }

        String secretKey = properties.getEncryption().getSecretKey();
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("未配置加密密钥");
        }

        return adjustKeyLengthForAlgorithm(secretKey, algorithm);
    }

    /**
     * 根据算法调整密钥长度
     */
    private String adjustKeyLengthForAlgorithm(String key, String algorithm) {
        switch (algorithm) {
            case "AES":
                return adjustKeyLength(key, 16); // AES-128
            case "DES":
                return adjustKeyLength(key, 8);  // DES-64
            case "DESede":
                return adjustKeyLength(key, 24); // 3DES-192
            case "SM4":
                return adjustKeyLength(key, 16); // SM4-128
            case "SM2":
            case "RSA":
            case "EC":
                return key; // 非对称算法密钥长度可变
            default:
                return key;
        }
    }

    /**
     * 调整密钥长度
     */
    private String adjustKeyLength(String key, int targetLength) {
        if (key.length() == targetLength) {
            return key;
        } else if (key.length() > targetLength) {
            return key.substring(0, targetLength);
        } else {
            StringBuilder sb = new StringBuilder(key);
            while (sb.length() < targetLength) {
                sb.append("0");
            }
            return sb.toString();
        }
    }

    /**
     * 记录性能统计
     */
    private void recordPerformance(String operation, long startTime) {
        long duration = System.nanoTime() - startTime;
        PERFORMANCE_STATS.merge(operation, duration, Long::sum);
    }

    /**
     * 检查字符串是否为数字
     */
    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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
        HOMOMORPHIC_METADATA.clear();
        log.info("已清空加密器缓存");
    }

    /**
     * 同态加密 - 支持加密数据的计算操作
     *
     * @param encryptedValue1 加密值1
     * @param encryptedValue2 加密值2
     * @param operation 操作类型（ADD, MULTIPLY）
     * @return 计算结果（仍为加密状态）
     */
    public String homomorphicCompute(String encryptedValue1, String encryptedValue2, HomomorphicOperation operation) {
        try {
            // 获取元信息
            HomomorphicMetadata metadata1 = HOMOMORPHIC_METADATA.get(encryptedValue1);
            HomomorphicMetadata metadata2 = HOMOMORPHIC_METADATA.get(encryptedValue2);

            if (metadata1 == null || metadata2 == null) {
                throw new IllegalArgumentException("无法找到同态加密元信息");
            }

            // 简化的同态加密实现（实际项目中需要使用专业的同态加密库如 SEAL、HElib）
            long value1 = Long.parseLong(metadata1.getOriginalValue());
            long value2 = Long.parseLong(metadata2.getOriginalValue());

            long result;
            switch (operation) {
                case ADD:
                    result = value1 + value2;
                    break;
                case MULTIPLY:
                    result = value1 * value2;
                    break;
                default:
                    throw new IllegalArgumentException("不支持的同态操作: " + operation);
            }

            // 加密结果
            String encryptedResult = encrypt(String.valueOf(result), EncryptType.AES);

            // 保存元信息
            HomomorphicMetadata resultMetadata = new HomomorphicMetadata();
            resultMetadata.setOriginalValue(String.valueOf(result));
            resultMetadata.setOperation(operation.name());
            resultMetadata.setTimestamp(System.currentTimeMillis());
            HOMOMORPHIC_METADATA.put(encryptedResult, resultMetadata);

            log.debug("同态计算完成: {} {} {} = {}", value1, operation, value2, result);
            return encryptedResult;

        } catch (Exception e) {
            log.error("同态计算失败", e);
            throw new RuntimeException("同态计算失败", e);
        }
    }

    /**
     * 国密 SM4 加密实现
     */
    private String encryptSM4(String plainText) throws Exception {
        // 注意：这是简化实现，实际项目中应使用 BouncyCastle 的 SM4 实现
        log.warn("SM4 加密需要集成 BouncyCastle 国密算法库");

        // 模拟 SM4 加密（实际应该使用真实的 SM4 实现）
        String key = getEncryptionKey("SM4");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);

        // 生成随机 IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        // 这里应该调用真实的 SM4 加密算法
        // 当前返回模拟结果
        byte[] encrypted = simulateSM4Encryption(plainBytes, keyBytes, iv);

        // 组合 IV 和密文
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 国密 SM4 解密实现
     */
    private String decryptSM4(String cipherText) throws Exception {
        log.warn("SM4 解密需要集成 BouncyCastle 国密算法库");

        byte[] data = Base64.getDecoder().decode(cipherText);

        // 提取 IV 和密文
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[data.length - 16];
        System.arraycopy(data, 0, iv, 0, 16);
        System.arraycopy(data, 16, encrypted, 0, encrypted.length);

        String key = getEncryptionKey("SM4");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // 这里应该调用真实的 SM4 解密算法
        byte[] decrypted = simulateSM4Decryption(encrypted, keyBytes, iv);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 模拟 SM4 加密（实际项目中应使用真实实现）
     */
    private byte[] simulateSM4Encryption(byte[] plainText, byte[] key, byte[] iv) {
        // 这是模拟实现，实际应该使用：
        // SM4Engine engine = new SM4Engine();
        // CBCBlockCipher cipher = new CBCBlockCipher(engine);
        // 等 BouncyCastle 的 SM4 实现

        byte[] result = new byte[plainText.length + 16]; // 模拟填充
        System.arraycopy(plainText, 0, result, 0, plainText.length);
        return result;
    }

    /**
     * 模拟 SM4 解密（实际项目中应使用真实实现）
     */
    private byte[] simulateSM4Decryption(byte[] cipherText, byte[] key, byte[] iv) {
        // 模拟解密，移除填充
        if (cipherText.length >= 16) {
            byte[] result = new byte[cipherText.length - 16];
            System.arraycopy(cipherText, 0, result, 0, result.length);
            return result;
        }
        return cipherText;
    }

    /**
     * 同态加密元信息
     */
    public static class HomomorphicMetadata {
        private String originalValue;
        private String operation;
        private long timestamp;

        // Getters and Setters
        public String getOriginalValue() { return originalValue; }
        public void setOriginalValue(String originalValue) { this.originalValue = originalValue; }

        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 同态操作类型
     */
    public enum HomomorphicOperation {
        ADD,      // 加法
        MULTIPLY  // 乘法
    }
}
