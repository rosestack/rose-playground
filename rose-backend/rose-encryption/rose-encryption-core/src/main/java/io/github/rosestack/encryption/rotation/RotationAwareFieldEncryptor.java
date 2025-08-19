package io.github.rosestack.encryption.rotation;

import com.antherd.smcrypto.sm2.Sm2;
import com.antherd.smcrypto.sm4.Sm4;
import io.github.rosestack.encryption.FieldEncryptor;
import io.github.rosestack.encryption.enums.EncryptType;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 支持密钥轮换的字段加密器
 *
 * <p>支持多版本密钥，能够用新密钥加密，用旧密钥解密
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RotationAwareFieldEncryptor implements FieldEncryptor {

    /**
     * 加密数据格式：{version}:{encryptedData}
     */
    private static final Pattern VERSIONED_DATA_PATTERN = Pattern.compile("^\\{([^}]+)}:(.+)$");

    private final KeyRotationManager keyRotationManager;

    @Override
    public String encrypt(String plainText, EncryptType encryptType) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            KeySpec currentKeySpec = keyRotationManager.getCurrentKeySpec();

            // 验证算法类型匹配
            if (currentKeySpec.getEncryptType() != encryptType) {
                throw new IllegalArgumentException(
                        "当前密钥规格算法类型不匹配: " + currentKeySpec.getEncryptType() + " vs " + encryptType);
            }

            String encryptedData = doEncrypt(plainText, encryptType, currentKeySpec);

            // 添加版本前缀
            return "{" + currentKeySpec.getVersion() + "}:" + encryptedData;

        } catch (Exception e) {
            log.error("字段加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("字段加密失败", e);
        }
    }

    @Override
    public String decrypt(String cipherText, EncryptType encryptType) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        try {
            // 解析版本信息
            Matcher matcher = VERSIONED_DATA_PATTERN.matcher(cipherText);

            if (matcher.matches()) {
                // 有版本信息的新格式
                String version = matcher.group(1);
                String encryptedData = matcher.group(2);

                KeySpec keySpec = keyRotationManager.getKeySpec(version);
                if (keySpec == null) {
                    throw new IllegalArgumentException("未找到密钥版本: " + version);
                }

                if (!keySpec.canDecrypt()) {
                    throw new IllegalArgumentException("密钥版本不可用于解密: " + version);
                }

                return doDecrypt(encryptedData, encryptType, keySpec);
            } else {
                // 旧格式，尝试用所有可用的密钥解密
                return decryptWithFallback(cipherText, encryptType);
            }

        } catch (Exception e) {
            log.error("字段解密失败: {}", e.getMessage(), e);
            return cipherText; // 解密失败时返回原文
        }
    }

    /**
     * 回退解密：尝试用所有可用的密钥解密
     */
    private String decryptWithFallback(String cipherText, EncryptType encryptType) {
        for (KeySpec keySpec : keyRotationManager.getDecryptableKeySpecs()) {
            if (keySpec.getEncryptType() == encryptType) {
                try {
                    return doDecrypt(cipherText, encryptType, keySpec);
                } catch (Exception e) {
                    log.debug("使用密钥版本 {} 解密失败，尝试下一个", keySpec.getVersion());
                }
            }
        }

        log.warn("所有密钥版本都无法解密数据");
        return cipherText;
    }

    /**
     * 执行加密
     */
    private String doEncrypt(String plainText, EncryptType encryptType, KeySpec keySpec) throws Exception {
        switch (encryptType) {
            case SM4:
                // SM4 需要16字节的密钥，转换为十六进制字符串
                byte[] sm4Key = keySpec.getSecretKeyBytes();
                if (sm4Key.length != 16) {
                    throw new IllegalArgumentException("SM4密钥长度必须为16字节，当前长度: " + sm4Key.length);
                }
                String hexKey = bytesToHex(sm4Key);
                return Sm4.encrypt(plainText, hexKey);
            case SM2:
                return Sm2.doEncrypt(plainText, keySpec.getSm2PublicKey());
            case RSA:
                return encryptWithRSA(plainText, keySpec.getPublicKey());
            case AES:
            case DES:
            case DES3:
                return encryptWithSymmetric(plainText, encryptType, keySpec.getSecretKeyBytes());
            default:
                throw new UnsupportedOperationException("不支持的加密算法: " + encryptType);
        }
    }

    /**
     * 执行解密
     */
    private String doDecrypt(String cipherText, EncryptType encryptType, KeySpec keySpec) throws Exception {
        switch (encryptType) {
            case SM4:
                // SM4 需要16字节的密钥，转换为十六进制字符串
                byte[] sm4Key = keySpec.getSecretKeyBytes();
                if (sm4Key.length != 16) {
                    throw new IllegalArgumentException("SM4密钥长度必须为16字节，当前长度: " + sm4Key.length);
                }
                String hexKey = bytesToHex(sm4Key);
                return Sm4.decrypt(cipherText, hexKey);
            case SM2:
                return Sm2.doDecrypt(cipherText, keySpec.getSm2PrivateKey());
            case RSA:
                return decryptWithRSA(cipherText, keySpec.getPrivateKey());
            case AES:
            case DES:
            case DES3:
                return decryptWithSymmetric(cipherText, encryptType, keySpec.getSecretKeyBytes());
            default:
                throw new UnsupportedOperationException("不支持的解密算法: " + encryptType);
        }
    }

    /**
     * 对称加密
     */
    private String encryptWithSymmetric(String plainText, EncryptType encryptType, byte[] keyBytes) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, encryptType.name());
        Cipher cipher = Cipher.getInstance(encryptType.name());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 对称解密
     */
    private String decryptWithSymmetric(String cipherText, EncryptType encryptType, byte[] keyBytes) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, encryptType.name());
        Cipher cipher = Cipher.getInstance(encryptType.name());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * RSA加密
     */
    private String encryptWithRSA(String plainText, String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * RSA解密
     */
    private String decryptWithRSA(String cipherText, String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 检查数据是否为新格式（包含版本信息）
     */
    public boolean isVersionedData(String data) {
        return data != null && VERSIONED_DATA_PATTERN.matcher(data).matches();
    }

    /**
     * 提取数据版本
     */
    public String extractVersion(String versionedData) {
        Matcher matcher = VERSIONED_DATA_PATTERN.matcher(versionedData);
        return matcher.matches() ? matcher.group(1) : null;
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
}
