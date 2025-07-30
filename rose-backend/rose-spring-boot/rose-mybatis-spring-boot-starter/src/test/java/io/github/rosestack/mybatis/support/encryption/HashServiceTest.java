package io.github.rosestack.mybatis.support.encryption;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.support.encryption.hash.HashService;
import io.github.rosestack.mybatis.support.encryption.hash.HashType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 哈希服务测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class HashServiceTest {

    private HashService hashService;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getEncryption().getHash().setEnabled(true);
        properties.getEncryption().getHash().setGlobalSalt("test-salt-2024");
        properties.getEncryption().getHash().setHmacKey("test-hmac-key-2024");
        
        hashService = new HashService(properties);
    }

    @Test
    void testSha256Hash() {
        String plainText = "13800138000";
        String hash1 = hashService.generateHash(plainText, HashType.SHA256);
        String hash2 = hashService.generateHash(plainText, HashType.SHA256);
        
        // 相同输入应该产生相同哈希
        assertEquals(hash1, hash2);
        
        // 哈希值不应该等于原文
        assertNotEquals(plainText, hash1);
        
        // 哈希值应该是64位十六进制字符串（SHA-256）
        assertEquals(64, hash1.length());
        assertTrue(hash1.matches("[0-9a-f]+"));
    }

    @Test
    void testSha512Hash() {
        String plainText = "admin@example.com";
        String hash1 = hashService.generateHash(plainText, HashType.SHA512);
        String hash2 = hashService.generateHash(plainText, HashType.SHA512);
        
        // 相同输入应该产生相同哈希
        assertEquals(hash1, hash2);
        
        // 哈希值应该是128位十六进制字符串（SHA-512）
        assertEquals(128, hash1.length());
        assertTrue(hash1.matches("[0-9a-f]+"));
    }

    @Test
    void testHmacSha256Hash() {
        String plainText = "13800138000";
        String hash1 = hashService.generateHash(plainText, HashType.HMAC_SHA256);
        String hash2 = hashService.generateHash(plainText, HashType.HMAC_SHA256);
        
        // 相同输入应该产生相同哈希
        assertEquals(hash1, hash2);
        
        // 哈希值不应该等于原文
        assertNotEquals(plainText, hash1);
        
        // HMAC-SHA256 产生64位十六进制字符串
        assertEquals(64, hash1.length());
        assertTrue(hash1.matches("[0-9a-f]+"));
    }

    @Test
    void testHmacSha512Hash() {
        String plainText = "admin@example.com";
        String hash1 = hashService.generateHash(plainText, HashType.HMAC_SHA512);
        String hash2 = hashService.generateHash(plainText, HashType.HMAC_SHA512);
        
        // 相同输入应该产生相同哈希
        assertEquals(hash1, hash2);
        
        // HMAC-SHA512 产生128位十六进制字符串
        assertEquals(128, hash1.length());
        assertTrue(hash1.matches("[0-9a-f]+"));
    }

    @Test
    void testDifferentInputsProduceDifferentHashes() {
        String hash1 = hashService.generateHash("13800138000", HashType.HMAC_SHA256);
        String hash2 = hashService.generateHash("13800138001", HashType.HMAC_SHA256);
        
        // 不同输入应该产生不同哈希
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testVerifyHash() {
        String plainText = "13800138000";
        String hash = hashService.generateHash(plainText, HashType.HMAC_SHA256);
        
        // 验证正确的哈希
        assertTrue(hashService.verifyHash(plainText, hash, HashType.HMAC_SHA256));
        
        // 验证错误的哈希
        assertFalse(hashService.verifyHash("13800138001", hash, HashType.HMAC_SHA256));
        
        // 验证空值
        assertFalse(hashService.verifyHash(null, hash, HashType.HMAC_SHA256));
        assertFalse(hashService.verifyHash(plainText, null, HashType.HMAC_SHA256));
    }

    @Test
    void testGenerateHashFieldName() {
        // 默认生成
        String hashFieldName1 = hashService.generateHashFieldName("phone", "");
        assertEquals("phone_hash", hashFieldName1);
        
        // 自定义字段名
        String hashFieldName2 = hashService.generateHashFieldName("phone", "custom_phone_hash");
        assertEquals("custom_phone_hash", hashFieldName2);
    }

    @Test
    void testHashDisabled() {
        // 禁用哈希功能
        properties.getEncryption().getHash().setEnabled(false);
        HashService disabledHashService = new HashService(properties);
        
        String plainText = "13800138000";
        String result = disabledHashService.generateHash(plainText, HashType.SHA256);
        
        // 禁用时应该返回原文
        assertEquals(plainText, result);
    }

    @Test
    void testEmptyInput() {
        // 空字符串
        String result1 = hashService.generateHash("", HashType.SHA256);
        assertEquals("", result1);
        
        // null
        String result2 = hashService.generateHash(null, HashType.SHA256);
        assertNull(result2);
    }

    @Test
    void testSaltEffect() {
        // 使用不同盐值应该产生不同哈希
        RoseMybatisProperties properties2 = new RoseMybatisProperties();
        properties2.getEncryption().getHash().setEnabled(true);
        properties2.getEncryption().getHash().setGlobalSalt("different-salt-2024");
        properties2.getEncryption().getHash().setHmacKey("test-hmac-key-2024");

        HashService hashService2 = new HashService(properties2);

        String plainText = "13800138000";
        String hash1 = hashService.generateHash(plainText, HashType.SHA256);
        String hash2 = hashService2.generateHash(plainText, HashType.SHA256);

        // 不同盐值应该产生不同哈希
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testGetDefaultHashType_WithHmac() {
        // 配置使用 HMAC
        properties.getEncryption().getHash().setAlgorithm("HMAC_SHA256");

        HashType defaultType = hashService.getDefaultHashType();
        assertEquals(HashType.HMAC_SHA256, defaultType);

        // 测试 SHA512 配置
        properties.getEncryption().getHash().setAlgorithm("HMAC_SHA512");
        defaultType = hashService.getDefaultHashType();
        assertEquals(HashType.HMAC_SHA512, defaultType);
    }

    @Test
    void testGetDefaultHashType_WithoutHmac() {
        // 配置不使用 HMAC
        properties.getEncryption().getHash().setAlgorithm("SHA256");

        HashType defaultType = hashService.getDefaultHashType();
        assertEquals(HashType.SHA256, defaultType);

        // 测试 SHA512 配置
        properties.getEncryption().getHash().setAlgorithm("SHA512");
        defaultType = hashService.getDefaultHashType();
        assertEquals(HashType.SHA512, defaultType);
    }

    @Test
    void testGenerateHashWithDefault() {
        // 测试使用默认算法生成哈希
        String plainText = "13800138000";
        String hash = hashService.generateHashWithDefault(plainText);

        assertNotNull(hash);
        assertNotEquals(plainText, hash);

        // 验证与指定默认算法的结果一致
        HashType defaultType = hashService.getDefaultHashType();
        String expectedHash = hashService.generateHash(plainText, defaultType);
        assertEquals(expectedHash, hash);
    }

    @Test
    void testAlgorithmConfiguration() {
        // 测试不同算法配置
        String plainText = "test@example.com";

        // 测试各种算法配置字符串
        String[] algorithms = {"SHA256", "sha256", "SHA512", "sha512", "HMAC_SHA256", "hmac_sha256"};

        for (String algorithm : algorithms) {
            properties.getEncryption().getHash().setAlgorithm(algorithm);

            HashType defaultType = hashService.getDefaultHashType();
            assertNotNull(defaultType);

            String hash = hashService.generateHashWithDefault(plainText);
            assertNotNull(hash);
            assertNotEquals(plainText, hash);
        }
    }

    @Test
    void testGenerateHashByField() {
        // 测试根据字段注解生成哈希
        String plainText = "13800138000";

        // 测试 phone 字段（默认 SHA256）
        String phoneHash = hashService.generateHashByField(plainText, TestEntity.class, "phone");
        assertNotNull(phoneHash);
        assertNotEquals(plainText, phoneHash);

        // 验证使用的是 SHA256 算法
        String expectedPhoneHash = hashService.generateHash(plainText, HashType.SHA256);
        assertEquals(expectedPhoneHash, phoneHash);

        // 测试 email 字段（明确指定 HMAC_SHA256）
        String emailHash = hashService.generateHashByField(plainText, TestEntity.class, "email");
        assertNotNull(emailHash);
        assertNotEquals(plainText, emailHash);

        // 验证使用的是 HMAC_SHA256 算法
        String expectedEmailHash = hashService.generateHash(plainText, HashType.HMAC_SHA256);
        assertEquals(expectedEmailHash, emailHash);
    }

    @Test
    void testGenerateHashByField_FieldNotFound() {
        String plainText = "test";

        assertThrows(IllegalArgumentException.class, () -> {
            hashService.generateHashByField(plainText, TestEntity.class, "nonExistentField");
        });
    }

    @Test
    void testGenerateHashByField_NotSearchable() {
        String plainText = "test";

        assertThrows(IllegalArgumentException.class, () -> {
            hashService.generateHashByField(plainText, TestEntity.class, "idCard");
        });
    }

    // 测试实体类
    static class TestEntity {
        @io.github.rosestack.mybatis.annotation.EncryptField(searchable = true)
        private String phone; // 默认使用 SHA256

        @io.github.rosestack.mybatis.annotation.EncryptField(searchable = true, hashType = HashType.HMAC_SHA256)
        private String email; // 明确指定 HMAC_SHA256

        @io.github.rosestack.mybatis.annotation.EncryptField // 不支持查询
        private String idCard;
    }
}
