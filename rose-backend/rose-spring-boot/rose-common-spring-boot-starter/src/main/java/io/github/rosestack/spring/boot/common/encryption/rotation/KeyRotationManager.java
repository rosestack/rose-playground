package io.github.rosestack.spring.boot.common.encryption.rotation;

import com.antherd.smcrypto.sm2.Keypair;
import com.antherd.smcrypto.sm2.Sm2;
import io.github.rosestack.spring.boot.common.encryption.enums.EncryptType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 密钥轮换管理器
 * <p>
 * 负责管理多版本密钥，支持密钥轮换功能
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class KeyRotationManager {

    /**
     * 密钥存储 - 版本号 -> 密钥规格
     */
    private final Map<String, KeySpec> keySpecs = new ConcurrentHashMap<>();

    /**
     * 当前活跃版本
     */
    private volatile String currentVersion;

    /**
     * 安全随机数生成器
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 注册密钥规格
     */
    public void registerKeySpec(KeySpec keySpec) {
        if (!keySpec.isValidForAlgorithm()) {
            throw new IllegalArgumentException("密钥规格不符合算法要求: " + keySpec.getEncryptType());
        }

        keySpecs.put(keySpec.getVersion(), keySpec);
        log.info("注册密钥规格: version={}, algorithm={}", keySpec.getVersion(), keySpec.getEncryptType());

        // 如果是活跃版本，更新当前版本
        if (keySpec.isActive()) {
            setCurrentVersion(keySpec.getVersion());
        }
    }

    /**
     * 获取当前活跃的密钥规格
     */
    public KeySpec getCurrentKeySpec() {
        if (currentVersion == null) {
            throw new IllegalStateException("未设置当前密钥版本");
        }
        return keySpecs.get(currentVersion);
    }

    /**
     * 根据版本获取密钥规格
     */
    public KeySpec getKeySpec(String version) {
        return keySpecs.get(version);
    }

    /**
     * 获取所有可用于解密的密钥规格
     */
    public List<KeySpec> getDecryptableKeySpecs() {
        return keySpecs.values().stream()
                .filter(KeySpec::canDecrypt)
                .collect(Collectors.toList());
    }

    /**
     * 设置当前版本
     */
    public void setCurrentVersion(String version) {
        KeySpec keySpec = keySpecs.get(version);
        if (keySpec == null) {
            throw new IllegalArgumentException("密钥版本不存在: " + version);
        }
        if (!keySpec.canEncrypt()) {
            throw new IllegalArgumentException("密钥版本不可用于加密: " + version);
        }

        this.currentVersion = version;
        log.info("切换到密钥版本: {}", version);
    }

    /**
     * 轮换到新版本
     */
    public String rotateToNewVersion(EncryptType encryptType) {
        String newVersion = generateNewVersion();
        KeySpec newKeySpec = generateKeySpec(newVersion, encryptType);

        // 将旧版本标记为非活跃
        if (currentVersion != null) {
            KeySpec oldKeySpec = keySpecs.get(currentVersion);
            if (oldKeySpec != null) {
                oldKeySpec.setActive(false);
            }
        }

        registerKeySpec(newKeySpec);
        log.info("密钥轮换完成: {} -> {}", currentVersion, newVersion);

        return newVersion;
    }

    /**
     * 废弃指定版本的密钥
     */
    public void deprecateVersion(String version) {
        KeySpec keySpec = keySpecs.get(version);
        if (keySpec != null) {
            keySpec.setDeprecated(true);
            keySpec.setActive(false);
            log.info("废弃密钥版本: {}", version);
        }
    }

    /**
     * 生成新的版本号
     */
    private String generateNewVersion() {
        return "v" + System.currentTimeMillis();
    }

    /**
     * 生成密钥规格
     */
    private KeySpec generateKeySpec(String version, EncryptType encryptType) {
        KeySpec.KeySpecBuilder builder = KeySpec.builder()
                .version(version)
                .encryptType(encryptType)
                .createdTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .active(true)
                .deprecated(false)
                .description("Auto-generated key for " + encryptType);

        // 根据算法类型生成相应的密钥
        switch (encryptType) {
            case AES:
                builder.secretKey(generateAESKey(256)); // 默认256位
                break;
            case DES:
                builder.secretKey(generateDESKey());
                break;
            case DES3:
                builder.secretKey(generate3DESKey());
                break;
            case SM4:
                builder.secretKey(generateSM4Key());
                break;
            case SM2:
                // SM2密钥对生成需要专门的库
                generateSM2KeyPair(builder);
                break;
            case RSA:
                // RSA密钥对生成
                generateRSAKeyPair(builder);
                break;
        }

        return builder.build();
    }

    /**
     * 生成AES密钥
     */
    private String generateAESKey(int keySize) {
        byte[] key = new byte[keySize / 8];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 生成DES密钥
     */
    private String generateDESKey() {
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 生成3DES密钥
     */
    private String generate3DESKey() {
        byte[] key = new byte[24];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 生成SM4密钥
     */
    private String generateSM4Key() {
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }



    /**
     * 生成SM2密钥对（需要SM2库支持）
     */
    private void generateSM2KeyPair(KeySpec.KeySpecBuilder builder) {
        try {
            Keypair keypair = Sm2.generateKeyPairHex();
            builder.sm2PublicKey(keypair.getPublicKey());
            builder.sm2PrivateKey(keypair.getPrivateKey());
        } catch (Exception e) {
            throw new RuntimeException("生成SM2密钥对失败", e);
        }
    }

    /**
     * 生成RSA密钥对
     */
    private void generateRSAKeyPair(KeySpec.KeySpecBuilder builder) {
        try {
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            java.security.KeyPair keyPair = keyGen.generateKeyPair();

            builder.publicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            builder.privateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 获取所有密钥版本信息
     */
    public Map<String, KeySpec> getAllKeySpecs() {
        return new HashMap<>(keySpecs);
    }

    /**
     * 清理过期的密钥
     */
    public void cleanupExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredVersions = keySpecs.entrySet().stream()
                .filter(entry -> {
                    KeySpec spec = entry.getValue();
                    return spec.getExpireTime() != null &&
                            now.isAfter(spec.getExpireTime().plusDays(30)); // 30天宽限期
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        expiredVersions.forEach(version -> {
            keySpecs.remove(version);
            log.info("清理过期密钥版本: {}", version);
        });
    }
}
