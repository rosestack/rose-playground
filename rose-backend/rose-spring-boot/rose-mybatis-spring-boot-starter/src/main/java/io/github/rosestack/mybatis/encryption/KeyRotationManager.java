package io.github.rosestack.mybatis.encryption;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 密钥轮换管理器
 * <p>
 * 支持密钥的自动轮换、版本管理和向后兼容性
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class KeyRotationManager {

    /**
     * 密钥版本存储
     */
    private final Map<String, KeyVersion> keyVersions = new ConcurrentHashMap<>();
    
    /**
     * 当前密钥版本计数器
     */
    private final AtomicInteger versionCounter = new AtomicInteger(1);
    
    /**
     * 密钥轮换间隔（小时）
     */
    private static final int ROTATION_INTERVAL_HOURS = 24;
    
    /**
     * 密钥保留版本数
     */
    private static final int MAX_KEY_VERSIONS = 5;

    /**
     * 获取当前密钥
     *
     * @param keyId 密钥标识
     * @return 当前密钥信息
     */
    public KeyVersion getCurrentKey(String keyId) {
        return keyVersions.computeIfAbsent(keyId, k -> {
            KeyVersion keyVersion = new KeyVersion();
            keyVersion.setKeyId(keyId);
            keyVersion.setVersion(versionCounter.getAndIncrement());
            keyVersion.setKeyValue(generateKey());
            keyVersion.setCreatedTime(LocalDateTime.now());
            keyVersion.setActive(true);
            
            log.info("创建新密钥版本: keyId={}, version={}", keyId, keyVersion.getVersion());
            return keyVersion;
        });
    }

    /**
     * 根据版本获取密钥
     *
     * @param keyId   密钥标识
     * @param version 密钥版本
     * @return 指定版本的密钥信息
     */
    public KeyVersion getKeyByVersion(String keyId, int version) {
        KeyVersion currentKey = keyVersions.get(keyId);
        if (currentKey != null && currentKey.getVersion() == version) {
            return currentKey;
        }
        
        // 从历史版本中查找
        return currentKey != null ? currentKey.getHistoryVersions().get(version) : null;
    }

    /**
     * 轮换密钥
     *
     * @param keyId 密钥标识
     * @return 新的密钥版本
     */
    public KeyVersion rotateKey(String keyId) {
        KeyVersion currentKey = keyVersions.get(keyId);
        if (currentKey == null) {
            return getCurrentKey(keyId);
        }

        // 将当前密钥标记为非活跃并保存到历史版本
        currentKey.setActive(false);
        
        // 创建新密钥版本
        KeyVersion newKey = new KeyVersion();
        newKey.setKeyId(keyId);
        newKey.setVersion(versionCounter.getAndIncrement());
        newKey.setKeyValue(generateKey());
        newKey.setCreatedTime(LocalDateTime.now());
        newKey.setActive(true);
        
        // 保存历史版本
        newKey.getHistoryVersions().put(currentKey.getVersion(), currentKey);
        
        // 清理过期版本
        cleanupOldVersions(newKey);
        
        // 更新当前密钥
        keyVersions.put(keyId, newKey);
        
        log.info("密钥轮换完成: keyId={}, oldVersion={}, newVersion={}", 
                keyId, currentKey.getVersion(), newKey.getVersion());
        
        return newKey;
    }

    /**
     * 定时密钥轮换
     */
    @Scheduled(fixedRate = ROTATION_INTERVAL_HOURS * 60 * 60 * 1000)
    public void scheduledKeyRotation() {
        log.info("开始定时密钥轮换检查");
        
        keyVersions.forEach((keyId, keyVersion) -> {
            LocalDateTime rotationTime = keyVersion.getCreatedTime().plusHours(ROTATION_INTERVAL_HOURS);
            if (LocalDateTime.now().isAfter(rotationTime)) {
                rotateKey(keyId);
            }
        });
        
        log.info("定时密钥轮换检查完成");
    }

    /**
     * 清理过期版本
     */
    private void cleanupOldVersions(KeyVersion keyVersion) {
        if (keyVersion.getHistoryVersions().size() > MAX_KEY_VERSIONS) {
            // 保留最新的几个版本，删除最旧的版本
            keyVersion.getHistoryVersions().entrySet()
                    .stream()
                    .sorted(Map.Entry.<Integer, KeyVersion>comparingByKey().reversed())
                    .skip(MAX_KEY_VERSIONS)
                    .forEach(entry -> {
                        keyVersion.getHistoryVersions().remove(entry.getKey());
                        log.info("清理过期密钥版本: keyId={}, version={}", 
                                keyVersion.getKeyId(), entry.getKey());
                    });
        }
    }

    /**
     * 生成新密钥
     */
    private String generateKey() {
        // 这里应该使用安全的密钥生成算法
        // 实际项目中可以集成 HSM 或云密钥管理服务
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取密钥统计信息
     */
    public KeyRotationStats getStats() {
        KeyRotationStats stats = new KeyRotationStats();
        stats.setTotalKeys(keyVersions.size());
        stats.setActiveKeys((int) keyVersions.values().stream().filter(KeyVersion::isActive).count());
        stats.setTotalVersions(keyVersions.values().stream()
                .mapToInt(kv -> 1 + kv.getHistoryVersions().size()).sum());
        return stats;
    }

    /**
     * 密钥版本信息
     */
    @Data
    public static class KeyVersion {
        private String keyId;
        private int version;
        private String keyValue;
        private LocalDateTime createdTime;
        private boolean active;
        private Map<Integer, KeyVersion> historyVersions = new ConcurrentHashMap<>();
    }

    /**
     * 密钥轮换统计信息
     */
    @Data
    public static class KeyRotationStats {
        private int totalKeys;
        private int activeKeys;
        private int totalVersions;
    }
}
