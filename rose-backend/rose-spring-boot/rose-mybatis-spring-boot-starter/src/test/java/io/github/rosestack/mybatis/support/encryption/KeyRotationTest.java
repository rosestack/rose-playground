package io.github.rosestack.mybatis.support.encryption;

import io.github.rosestack.mybatis.support.encryption.rotation.KeyRotationManager;
import io.github.rosestack.mybatis.support.encryption.rotation.KeySpec;
import io.github.rosestack.mybatis.support.encryption.rotation.RotationAwareFieldEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密钥轮换功能测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class KeyRotationTest {

    private KeyRotationManager keyRotationManager;
    private RotationAwareFieldEncryptor encryptor;

    @BeforeEach
    void setUp() {
        keyRotationManager = new KeyRotationManager();
        encryptor = new RotationAwareFieldEncryptor(keyRotationManager);
    }

    @Test
    @DisplayName("测试AES密钥规格验证")
    void testAESKeySpecValidation() {
        // 测试有效的AES密钥长度
        KeySpec validAES128 = createAESKeySpec("v1", 128);
        assertTrue(validAES128.isValidForAlgorithm());

        KeySpec validAES256 = createAESKeySpec("v2", 256);
        assertTrue(validAES256.isValidForAlgorithm());

        // 测试无效的AES密钥长度
        KeySpec invalidAES = createAESKeySpec("v3", 120); // 无效长度
        assertFalse(invalidAES.isValidForAlgorithm());
    }

    @Test
    @DisplayName("测试DES密钥规格验证")
    void testDESKeySpecValidation() {
        // 测试有效的DES密钥长度
        KeySpec validDES = createDESKeySpec("v1");
        assertTrue(validDES.isValidForAlgorithm());

        // 测试无效的DES密钥长度
        KeySpec invalidDES = createInvalidDESKeySpec("v2");
        assertFalse(invalidDES.isValidForAlgorithm());
    }

    @Test
    @DisplayName("测试密钥轮换管理")
    void testKeyRotationManager() {
        // 注册第一个密钥版本
        KeySpec keyV1 = createAESKeySpec("v1", 256);
        keyRotationManager.registerKeySpec(keyV1);

        assertEquals("v1", keyRotationManager.getCurrentKeySpec().getVersion());

        // 轮换到新版本
        String newVersion = keyRotationManager.rotateToNewVersion(EncryptType.AES);
        assertNotNull(newVersion);
        assertNotEquals("v1", newVersion);

        // 验证新版本成为当前版本
        assertEquals(newVersion, keyRotationManager.getCurrentKeySpec().getVersion());

        // 验证旧版本仍可用于解密
        KeySpec oldKeySpec = keyRotationManager.getKeySpec("v1");
        assertNotNull(oldKeySpec);
        assertTrue(oldKeySpec.canDecrypt());
        assertFalse(oldKeySpec.isActive()); // 不再是活跃版本
    }

    @Test
    @DisplayName("测试支持轮换的加密解密")
    void testRotationAwareEncryptionDecryption() {
        // 注册第一个密钥版本
        KeySpec keyV1 = createAESKeySpec("v1", 256);
        keyRotationManager.registerKeySpec(keyV1);

        String plainText = "敏感数据测试";

        // 使用v1加密
        String encrypted = encryptor.encrypt(plainText, EncryptType.AES);
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("{v1}:"));

        // 解密应该成功
        String decrypted = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(plainText, decrypted);

        // 轮换到新版本
        String newVersion = keyRotationManager.rotateToNewVersion(EncryptType.AES);

        // 新数据用新密钥加密
        String newEncrypted = encryptor.encrypt(plainText, EncryptType.AES);
        assertTrue(newEncrypted.startsWith("{" + newVersion + "}:"));

        // 旧数据仍能解密
        String oldDecrypted = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(plainText, oldDecrypted);

        // 新数据也能解密
        String newDecrypted = encryptor.decrypt(newEncrypted, EncryptType.AES);
        assertEquals(plainText, newDecrypted);
    }

    @Test
    @DisplayName("测试版本数据格式解析")
    void testVersionedDataParsing() {
        assertTrue(encryptor.isVersionedData("{v1}:encryptedData"));
        assertFalse(encryptor.isVersionedData("plainEncryptedData"));

        assertEquals("v1", encryptor.extractVersion("{v1}:encryptedData"));
        assertNull(encryptor.extractVersion("plainEncryptedData"));
    }

    @Test
    @DisplayName("测试SM4密钥规格验证")
    void testSM4KeySpecValidation() {
        // 测试有效的SM4密钥长度
        KeySpec validSM4 = createSM4KeySpec("v1");
        assertTrue(validSM4.isValidForAlgorithm());

        // 测试无效的SM4密钥长度
        KeySpec invalidSM4 = createInvalidSM4KeySpec("v2");
        assertFalse(invalidSM4.isValidForAlgorithm());
    }

    @Test
    @DisplayName("测试SM2密钥规格验证")
    void testSM2KeySpecValidation() {
        // 测试有效的SM2密钥对
        KeySpec validSM2 = createSM2KeySpec("v1");
        assertTrue(validSM2.isValidForAlgorithm());

        // 测试无效的SM2密钥（缺少私钥）
        KeySpec invalidSM2 = createInvalidSM2KeySpec("v2");
        assertFalse(invalidSM2.isValidForAlgorithm());
    }

    @Test
    @DisplayName("测试SM4加密解密")
    void testSM4EncryptionDecryption() {
        // 注册SM4密钥版本
        KeySpec sm4Key = createSM4KeySpec("v1");
        keyRotationManager.registerKeySpec(sm4Key);

        String plainText = "SM4加密测试数据";

        // 使用SM4加密
        String encrypted = encryptor.encrypt(plainText, EncryptType.SM4);
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("{v1}:"));

        // 解密应该成功
        String decrypted = encryptor.decrypt(encrypted, EncryptType.SM4);
        assertEquals(plainText, decrypted);

        // 轮换到新版本
        String newVersion = keyRotationManager.rotateToNewVersion(EncryptType.SM4);

        // 新数据用新密钥加密
        String newEncrypted = encryptor.encrypt(plainText, EncryptType.SM4);
        assertTrue(newEncrypted.startsWith("{" + newVersion + "}:"));

        // 旧数据仍能解密
        String oldDecrypted = encryptor.decrypt(encrypted, EncryptType.SM4);
        assertEquals(plainText, oldDecrypted);

        // 新数据也能解密
        String newDecrypted = encryptor.decrypt(newEncrypted, EncryptType.SM4);
        assertEquals(plainText, newDecrypted);
    }

    @Test
    @DisplayName("测试密钥废弃功能")
    void testKeyDeprecation() {
        // 注册密钥版本
        KeySpec keyV1 = createAESKeySpec("v1", 256);
        keyRotationManager.registerKeySpec(keyV1);

        // 加密数据
        String plainText = "测试数据";
        String encrypted = encryptor.encrypt(plainText, EncryptType.AES);

        // 轮换到新版本
        keyRotationManager.rotateToNewVersion(EncryptType.AES);

        // 废弃旧版本
        keyRotationManager.deprecateVersion("v1");

        // 废弃的版本不能用于加密，也不能解密
        KeySpec deprecatedKey = keyRotationManager.getKeySpec("v1");
        assertFalse(deprecatedKey.canEncrypt());
        assertFalse(deprecatedKey.canDecrypt()); // 废弃后也不能解密

        // 加密器会尝试用其他可用密钥解密，但由于v1已废弃，会解密失败
        // 解密失败时返回原文
        String result = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(encrypted, result); // 解密失败，返回原文
    }

    /**
     * 创建AES密钥规格
     */
    private KeySpec createAESKeySpec(String version, int keySize) {
        byte[] key = new byte[keySize / 8];
        // 填充测试密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.AES)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建DES密钥规格
     */
    private KeySpec createDESKeySpec(String version) {
        byte[] key = new byte[8]; // DES需要8字节密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.DES)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建无效的DES密钥规格
     */
    private KeySpec createInvalidDESKeySpec(String version) {
        byte[] key = new byte[16]; // DES不支持16字节密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.DES)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建SM4密钥规格
     */
    private KeySpec createSM4KeySpec(String version) {
        byte[] key = new byte[16]; // SM4需要16字节密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.SM4)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建无效的SM4密钥规格
     */
    private KeySpec createInvalidSM4KeySpec(String version) {
        byte[] key = new byte[12]; // SM4不支持12字节密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.SM4)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建SM2密钥规格
     */
    private KeySpec createSM2KeySpec(String version) {
        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.SM2)
                .sm2PublicKey("test-sm2-public-key")
                .sm2PrivateKey("test-sm2-private-key")
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建无效的SM2密钥规格（缺少私钥）
     */
    private KeySpec createInvalidSM2KeySpec(String version) {
        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.SM2)
                .sm2PublicKey("test-sm2-public-key")
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .build();
    }
}
