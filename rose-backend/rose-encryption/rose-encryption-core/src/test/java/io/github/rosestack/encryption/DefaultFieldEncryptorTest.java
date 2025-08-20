package io.github.rosestack.encryption;

import io.github.rosestack.encryption.enums.EncryptType;
import io.github.rosestack.encryption.exception.EncryptionException;
import io.github.rosestack.encryption.monitor.EncryptionMonitorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 默认字段加密器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("默认字段加密器测试")
class DefaultFieldEncryptorTest {

    private static final String VALID_SECRET_KEY = "MySecretKey12345MySecretKey12345"; // 32字符
    private static final String WEAK_SECRET_KEY = "password12345678"; // 弱密钥
    private static final String SHORT_SECRET_KEY = "short"; // 短密钥
    private static final String TEST_PLAIN_TEXT = "Hello, World!";

    private DefaultFieldEncryptor encryptor;
    private DefaultFieldEncryptor failSafeEncryptor;

    @BeforeEach
    void setUp() {
        encryptor = new DefaultFieldEncryptor(VALID_SECRET_KEY, true);
        failSafeEncryptor = new DefaultFieldEncryptor(VALID_SECRET_KEY, false);
    }

    @Test
    @DisplayName("正常加密解密测试")
    void testNormalEncryptDecrypt() {
        String encrypted = encryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.AES);
        assertNotNull(encrypted);
        assertNotEquals(TEST_PLAIN_TEXT, encrypted);

        String decrypted = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(TEST_PLAIN_TEXT, decrypted);
    }

    @Test
    @DisplayName("空值处理测试")
    void testNullAndEmptyValues() {
        // 空字符串
        assertEquals("", encryptor.encrypt("", EncryptType.AES));
        assertEquals("", encryptor.decrypt("", EncryptType.AES));

        // null值
        assertNull(encryptor.encrypt(null, EncryptType.AES));
        assertNull(encryptor.decrypt(null, EncryptType.AES));

        // 空白字符串
        assertEquals("   ", encryptor.encrypt("   ", EncryptType.AES));
    }

    @Test
    @DisplayName("短密钥抛出异常")
    void testShortSecretKeyThrowsException() {
        DefaultFieldEncryptor shortKeyEncryptor = new DefaultFieldEncryptor(SHORT_SECRET_KEY, true);

        assertThrows(EncryptionException.class, () ->
            shortKeyEncryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.AES));
    }

    @Test
    @DisplayName("空密钥抛出异常")
    void testEmptySecretKeyThrowsException() {
        DefaultFieldEncryptor emptyKeyEncryptor = new DefaultFieldEncryptor("", true);

        assertThrows(EncryptionException.class, () ->
            emptyKeyEncryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.AES));

        DefaultFieldEncryptor nullKeyEncryptor = new DefaultFieldEncryptor(null, true);

        assertThrows(EncryptionException.class, () ->
            nullKeyEncryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.AES));
    }

    @Test
    @DisplayName("弱密钥警告测试")
    void testWeakKeyWarning() {
        // 弱密钥应该能正常工作，但会有警告日志
        DefaultFieldEncryptor weakKeyEncryptor = new DefaultFieldEncryptor(WEAK_SECRET_KEY, true);

        assertDoesNotThrow(() -> {
            String encrypted = weakKeyEncryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.AES);
            String decrypted = weakKeyEncryptor.decrypt(encrypted, EncryptType.AES);
            assertEquals(TEST_PLAIN_TEXT, decrypted);
        });
    }

    @Test
    @DisplayName("失败安全模式测试")
    void testFailSafeMode() {
        // 使用无效的密文测试失败安全模式
        String invalidCipherText = "invalid_cipher_text";

        // failOnError = false 时应该返回原始值而不抛出异常
        String result = failSafeEncryptor.decrypt(invalidCipherText, EncryptType.AES);
        assertEquals(invalidCipherText, result);

        // failOnError = true 时应该抛出异常
        assertThrows(EncryptionException.class, () ->
            encryptor.decrypt(invalidCipherText, EncryptType.AES));
    }

    @Test
    @DisplayName("多种加密类型测试")
    void testMultipleEncryptionTypes() {
        EncryptType[] types = {EncryptType.AES, EncryptType.DES, EncryptType.DES3};

        for (EncryptType type : types) {
            String encrypted = encryptor.encrypt(TEST_PLAIN_TEXT, type);
            assertNotNull(encrypted);
            assertNotEquals(TEST_PLAIN_TEXT, encrypted);

            String decrypted = encryptor.decrypt(encrypted, type);
            assertEquals(TEST_PLAIN_TEXT, decrypted);
        }
    }

    @Test
    @DisplayName("性能统计集成测试")
    void testPerformanceStatsIntegration() {
        // 清空之前的统计
		EncryptionMonitorManager.getInstance().clearCache();

        // 执行一些操作
        encryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.AES);
        encryptor.encrypt(TEST_PLAIN_TEXT, EncryptType.DES);

        var stats = EncryptionMonitorManager.getInstance().getPerformanceStats();
        assertNotNull(stats);
        assertFalse(stats.isEmpty());

        String cacheStats = EncryptionMonitorManager.getInstance().getCacheStats();
        assertNotNull(cacheStats);
        assertTrue(cacheStats.contains("性能计数器"));
    }

    @Test
    @DisplayName("长文本加密测试")
    void testLongTextEncryption() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("这是一个很长的测试文本，用于测试加密算法对长文本的处理能力。");
        }

        String plainText = longText.toString();
        String encrypted = encryptor.encrypt(plainText, EncryptType.AES);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("特殊字符加密测试")
    void testSpecialCharactersEncryption() {
        String specialText = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\n\t\r测试中文🎉🔐";

        String encrypted = encryptor.encrypt(specialText, EncryptType.AES);
        assertNotNull(encrypted);
        assertNotEquals(specialText, encrypted);

        String decrypted = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(specialText, decrypted);
    }

    @Test
    @DisplayName("并发安全测试")
    void testConcurrentSafety() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String text = "Thread-" + threadIndex + "-Operation-" + j;
                        String encrypted = encryptor.encrypt(text, EncryptType.AES);
                        String decrypted = encryptor.decrypt(encrypted, EncryptType.AES);
                        if (!text.equals(decrypted)) {
                            results[threadIndex] = false;
                            return;
                        }
                    }
                    results[threadIndex] = true;
                } catch (Exception e) {
                    results[threadIndex] = false;
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 检查结果
        for (boolean result : results) {
            assertTrue(result, "并发测试失败");
        }
    }
}
