package io.github.rosestack.spring.boot.common.encryption;

import io.github.rosestack.spring.boot.common.config.RoseCommonProperties;
import io.github.rosestack.spring.boot.common.encryption.enums.EncryptType;
import io.github.rosestack.spring.boot.common.encryption.rotation.AutoKeyRotationScheduler;
import io.github.rosestack.spring.boot.common.encryption.rotation.KeyRotationManager;
import io.github.rosestack.spring.boot.common.encryption.rotation.KeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自动密钥轮换功能测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AutoKeyRotationTest {

    private KeyRotationManager keyRotationManager;
    private AutoKeyRotationScheduler autoRotationScheduler;
    private RoseCommonProperties properties;

    @BeforeEach
    void setUp() {
        keyRotationManager = new KeyRotationManager();

        // 设置测试配置
        properties = new RoseCommonProperties();
        properties.getEncryption().getKeyRotation().setEnabled(true);
        properties.getEncryption().getKeyRotation().setAutoRotationDays(1); // 1天轮换用于测试
        properties.getEncryption().getKeyRotation().setKeyRetentionDays(7);
        properties.getEncryption().getKeyRotation().setAutoCleanup(true);

        autoRotationScheduler = new AutoKeyRotationScheduler(keyRotationManager, properties);
    }

    @Test
    @DisplayName("测试自动轮换状态检查")
    void testRotationStatusCheck() {
        // 初始状态：没有活跃密钥
        AutoKeyRotationScheduler.RotationStatus status = autoRotationScheduler.getRotationStatus();
        assertEquals("NO_ACTIVE_KEY", status.getStatus());

        // 注册一个新密钥
        KeySpec keySpec = createTestKeySpec("v1", LocalDateTime.now().minusDays(2)); // 2天前的密钥
        keyRotationManager.registerKeySpec(keySpec);

        // 检查状态：需要轮换
        status = autoRotationScheduler.getRotationStatus();
        assertEquals("NEEDS_ROTATION", status.getStatus());
        assertTrue(status.isNeedsRotation());
        assertEquals("v1", status.getCurrentVersion());
    }

    @Test
    @DisplayName("测试下次轮换时间计算")
    void testNextRotationTime() {
        // 没有密钥时
        assertNull(autoRotationScheduler.getNextRotationTime());

        // 注册密钥
        LocalDateTime activeTime = LocalDateTime.now();
        KeySpec keySpec = createTestKeySpec("v1", activeTime);
        keyRotationManager.registerKeySpec(keySpec);

        // 检查下次轮换时间
        LocalDateTime nextTime = autoRotationScheduler.getNextRotationTime();
        assertNotNull(nextTime);
        assertEquals(activeTime.plusDays(1), nextTime); // 配置的轮换周期是1天
    }

    @Test
    @DisplayName("测试手动触发轮换检查")
    void testManualTriggerCheck() {
        // 注册一个需要轮换的密钥
        KeySpec oldKey = createTestKeySpec("v1", LocalDateTime.now().minusDays(2));
        keyRotationManager.registerKeySpec(oldKey);

        // 手动触发检查
        assertDoesNotThrow(() -> autoRotationScheduler.triggerManualCheck());

        // 验证是否生成了新密钥
        KeySpec currentKey = keyRotationManager.getCurrentKeySpec();
        assertNotEquals("v1", currentKey.getVersion());
        assertTrue(currentKey.isActive());
    }

    @Test
    @DisplayName("测试过期密钥清理")
    void testExpiredKeyCleanup() {
        // 注册多个密钥版本
        KeySpec oldKey1 = createTestKeySpec("v1", LocalDateTime.now().minusDays(10));
        KeySpec oldKey2 = createTestKeySpec("v2", LocalDateTime.now().minusDays(5));
        KeySpec currentKey = createTestKeySpec("v3", LocalDateTime.now());

        oldKey1.setActive(false);
        oldKey2.setActive(false);

        keyRotationManager.registerKeySpec(oldKey1);
        keyRotationManager.registerKeySpec(oldKey2);
        keyRotationManager.registerKeySpec(currentKey);

        // 触发清理
        autoRotationScheduler.triggerManualCheck();

        // 验证过期密钥被废弃
        KeySpec key1 = keyRotationManager.getKeySpec("v1");
        assertTrue(key1.isDeprecated()); // 10天前的密钥应该被废弃

        KeySpec key2 = keyRotationManager.getKeySpec("v2");
        assertFalse(key2.isDeprecated()); // 5天前的密钥还在保留期内

        KeySpec key3 = keyRotationManager.getKeySpec("v3");
        assertTrue(key3.isActive()); // 当前密钥保持活跃
    }

    @Test
    @DisplayName("测试SM4密钥自动轮换")
    void testSM4AutoRotation() {
        // 注册SM4密钥
        KeySpec sm4Key = createSM4KeySpec("v1", LocalDateTime.now().minusDays(2));
        keyRotationManager.registerKeySpec(sm4Key);

        // 触发轮换
        autoRotationScheduler.triggerManualCheck();

        // 验证新密钥
        KeySpec newKey = keyRotationManager.getCurrentKeySpec();
        assertNotEquals("v1", newKey.getVersion());
        assertEquals(EncryptType.SM4, newKey.getEncryptType());
        assertTrue(newKey.isValidForAlgorithm());
    }

    @Test
    @DisplayName("测试轮换配置禁用时的行为")
    void testDisabledAutoRotation() {
        // 注册需要轮换的密钥
        KeySpec oldKey = createTestKeySpec("v1", LocalDateTime.now().minusDays(2));
        keyRotationManager.registerKeySpec(oldKey);

        // 禁用自动轮换（在注册密钥后）
        properties.getEncryption().getKeyRotation().setEnabled(false);

        // 触发检查（应该不会轮换）
        autoRotationScheduler.triggerManualCheck();

        // 验证密钥没有变化
        KeySpec currentKey = keyRotationManager.getCurrentKeySpec();
        assertEquals("v1", currentKey.getVersion());
    }

    @Test
    @DisplayName("测试轮换异常处理")
    void testRotationErrorHandling() {
        // 注册一个正常的密钥先
        KeySpec normalKey = createTestKeySpec("v1", LocalDateTime.now().minusDays(2));
        keyRotationManager.registerKeySpec(normalKey);

        // 触发检查应该不会抛出异常，即使遇到错误也会记录日志
        assertDoesNotThrow(() -> autoRotationScheduler.triggerManualCheck());

        // 验证轮换成功（因为密钥是有效的）
        KeySpec currentKey = keyRotationManager.getCurrentKeySpec();
        assertNotEquals("v1", currentKey.getVersion());
    }

    /**
     * 创建测试用的密钥规格
     */
    private KeySpec createTestKeySpec(String version, LocalDateTime activeTime) {
        byte[] key = new byte[32]; // 256位AES密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.AES)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(activeTime)
                .activeTime(activeTime)
                .active(true)
                .deprecated(false)
                .build();
    }

    /**
     * 创建SM4测试密钥规格
     */
    private KeySpec createSM4KeySpec(String version, LocalDateTime activeTime) {
        byte[] key = new byte[16]; // 128位SM4密钥
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }

        return KeySpec.builder()
                .version(version)
                .encryptType(EncryptType.SM4)
                .secretKey(Base64.getEncoder().encodeToString(key))
                .createdTime(activeTime)
                .activeTime(activeTime)
                .active(true)
                .deprecated(false)
                .build();
    }
}
