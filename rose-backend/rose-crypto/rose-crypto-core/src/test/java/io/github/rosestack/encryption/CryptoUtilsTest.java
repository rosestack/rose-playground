package io.github.rosestack.crypto;

import io.github.rosestack.crypto.enums.EncryptType;
import io.github.rosestack.crypto.exception.CryptoException;
import io.github.rosestack.crypto.monitor.CryptoMonitorManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 加密工具类测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("加密工具类测试")
class CryptoUtilsTest {

    private static final String TEST_PLAIN_TEXT = "Hello, World! 这是一个测试文本。";
    private static final String TEST_SECRET_KEY = "MySecretKey12345MySecretKey12345"; // 32字符

    @Test
    @DisplayName("AES加密解密测试")
    void testAESEncryptDecrypt() {
        // 加密
        String encrypted = CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);
        assertNotNull(encrypted);
        assertNotEquals(TEST_PLAIN_TEXT, encrypted);

        // 解密
        String decrypted = CryptoUtils.decrypt(encrypted, EncryptType.AES, TEST_SECRET_KEY);
        assertEquals(TEST_PLAIN_TEXT, decrypted);
    }

    @ParameterizedTest
    @EnumSource(value = EncryptType.class, names = {"AES", "DES", "DES3"})
    @DisplayName("多种加密算法测试")
    void testMultipleEncryptionTypes(EncryptType encryptType) {
        String encrypted = CryptoUtils.encrypt(TEST_PLAIN_TEXT, encryptType, TEST_SECRET_KEY);
        assertNotNull(encrypted);
        assertNotEquals(TEST_PLAIN_TEXT, encrypted);

        String decrypted = CryptoUtils.decrypt(encrypted, encryptType, TEST_SECRET_KEY);
        assertEquals(TEST_PLAIN_TEXT, decrypted);
    }

    @Test
    @DisplayName("空值处理测试")
    void testNullAndEmptyValues() {
        // 空字符串
        assertEquals("", CryptoUtils.encrypt("", EncryptType.AES, TEST_SECRET_KEY));
        assertEquals("", CryptoUtils.decrypt("", EncryptType.AES, TEST_SECRET_KEY));

        // null值
        assertNull(CryptoUtils.encrypt(null, EncryptType.AES, TEST_SECRET_KEY));
        assertNull(CryptoUtils.decrypt(null, EncryptType.AES, TEST_SECRET_KEY));
    }

    @Test
    @DisplayName("密钥为空时抛出异常")
    void testEmptySecretKeyThrowsException() {
        assertThrows(CryptoException.class, () ->
            CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, ""));

        assertThrows(CryptoException.class, () ->
            CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, null));
    }

    @Test
    @DisplayName("SM4加密测试（需要JavaScript引擎）")
    void testSM4EncryptionWithJavaScriptEngine() {
        try {
            String encrypted = CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.SM4, TEST_SECRET_KEY);
            String decrypted = CryptoUtils.decrypt(encrypted, EncryptType.SM4, TEST_SECRET_KEY);
            assertEquals(TEST_PLAIN_TEXT, decrypted);
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            // SM4 需要 JavaScript 引擎，在测试环境中可能不可用
            System.out.println("SM4测试跳过：缺少JavaScript引擎支持");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"短文本", "这是一个中等长度的测试文本，包含中文字符",
                           "This is a very long text that contains multiple sentences and should test the encryption algorithm's ability to handle longer inputs without any issues."})
    @DisplayName("不同长度文本加密测试")
    void testDifferentTextLengths(String text) {
        String encrypted = CryptoUtils.encrypt(text, EncryptType.AES, TEST_SECRET_KEY);
        assertNotNull(encrypted);

        String decrypted = CryptoUtils.decrypt(encrypted, EncryptType.AES, TEST_SECRET_KEY);
        assertEquals(text, decrypted);
    }

    @Test
    @DisplayName("性能统计测试")
    void testPerformanceStats() {
        // 执行一些加密操作
        CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);
        CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.DES, TEST_SECRET_KEY);

		Map<String, Long> stats = CryptoMonitorManager.getInstance().getPerformanceStats();
        assertNotNull(stats);
        assertFalse(stats.isEmpty());

        // 清空统计
        CryptoMonitorManager.getInstance().clearCache();
		Map<String, Long> emptyStats = CryptoMonitorManager.getInstance().getPerformanceStats();
        assertTrue(emptyStats.isEmpty());
    }

    @Test
    @DisplayName("批量加密解密测试")
    void testBatchEncryptDecrypt() {
        String[] plainTexts = {"Text1", "Text2", "Text3"};

        String[] encrypted = CryptoUtils.encryptBatch(plainTexts, EncryptType.AES, TEST_SECRET_KEY);
        assertNotNull(encrypted);
        assertEquals(plainTexts.length, encrypted.length);

        String[] decrypted = CryptoUtils.decryptBatch(encrypted, EncryptType.AES, TEST_SECRET_KEY);
        assertNotNull(decrypted);
        assertArrayEquals(plainTexts, decrypted);
    }

    @Test
    @DisplayName("加密结果随机性测试")
    void testEncryptionRandomness() {
        // AES-GCM 每次加密应该产生不同的结果（因为IV是随机的）
        String encrypted1 = CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);
        String encrypted2 = CryptoUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);

        assertNotEquals(encrypted1, encrypted2, "相同明文的两次加密结果应该不同（由于随机IV）");

        // 但解密结果应该相同
        String decrypted1 = CryptoUtils.decrypt(encrypted1, EncryptType.AES, TEST_SECRET_KEY);
        String decrypted2 = CryptoUtils.decrypt(encrypted2, EncryptType.AES, TEST_SECRET_KEY);

        assertEquals(TEST_PLAIN_TEXT, decrypted1);
        assertEquals(TEST_PLAIN_TEXT, decrypted2);
    }
}
