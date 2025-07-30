package io.github.rosestack.mybatis.support.encryption.rotation;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 自动密钥轮换调度器
 * <p>
 * 负责定期检查密钥状态并执行自动轮换
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.mybatis.encryption.key-rotation", name = "enabled", havingValue = "true")
public class AutoKeyRotationScheduler {

    private final KeyRotationManager keyRotationManager;
    private final RoseMybatisProperties properties;

    /**
     * 每小时检查一次密钥状态
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void checkKeyRotation() {
        try {
            log.debug("开始检查密钥轮换状态");

            // 检查是否启用自动轮换
            if (!properties.getEncryption().getKeyRotation().isEnabled()) {
                log.debug("自动密钥轮换已禁用，跳过检查");
                return;
            }

            KeySpec currentKey = keyRotationManager.getCurrentKeySpec();
            if (currentKey == null) {
                log.warn("未找到当前活跃密钥，跳过轮换检查");
                return;
            }

            // 检查是否需要轮换
            if (shouldRotateKey(currentKey)) {
                performAutoRotation(currentKey);
            }

            // 检查是否需要清理过期密钥
            if (properties.getEncryption().getKeyRotation().isAutoCleanup()) {
                cleanupExpiredKeys();
            }

        } catch (Exception e) {
            log.error("自动密钥轮换检查失败", e);
        }
    }

    /**
     * 判断是否需要轮换密钥
     */
    private boolean shouldRotateKey(KeySpec currentKey) {
        LocalDateTime activeTime = currentKey.getActiveTime();
        if (activeTime == null) {
            log.warn("密钥版本 {} 没有激活时间，跳过轮换检查", currentKey.getVersion());
            return false;
        }

        int rotationDays = properties.getEncryption().getKeyRotation().getAutoRotationDays();
        LocalDateTime rotationTime = activeTime.plusDays(rotationDays);
        
        boolean shouldRotate = LocalDateTime.now().isAfter(rotationTime);
        
        if (shouldRotate) {
            long daysSinceActive = ChronoUnit.DAYS.between(activeTime, LocalDateTime.now());
            log.info("密钥版本 {} 已激活 {} 天，达到轮换周期 {} 天，需要轮换", 
                currentKey.getVersion(), daysSinceActive, rotationDays);
        }
        
        return shouldRotate;
    }

    /**
     * 执行自动轮换
     */
    private void performAutoRotation(KeySpec currentKey) {
        try {
            log.info("开始自动密钥轮换，当前版本: {}", currentKey.getVersion());
            
            String newVersion = keyRotationManager.rotateToNewVersion(
                currentKey.getEncryptType()
            );
            
            log.info("自动密钥轮换完成: {} -> {}", currentKey.getVersion(), newVersion);
            
            // 发送轮换通知（可以扩展为发送邮件、消息等）
            sendRotationNotification(currentKey.getVersion(), newVersion);
            
        } catch (Exception e) {
            log.error("自动密钥轮换失败，当前版本: {}", currentKey.getVersion(), e);
            
            // 发送失败通知
            sendRotationFailureNotification(currentKey.getVersion(), e.getMessage());
        }
    }

    /**
     * 清理过期密钥
     */
    private void cleanupExpiredKeys() {
        try {
            log.debug("开始清理过期密钥");
            
            Map<String, KeySpec> allKeys = keyRotationManager.getAllKeySpecs();
            int retentionDays = properties.getEncryption().getKeyRotation().getKeyRetentionDays();
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            
            int cleanedCount = 0;
            for (Map.Entry<String, KeySpec> entry : allKeys.entrySet()) {
                String version = entry.getKey();
                KeySpec keySpec = entry.getValue();
                
                // 跳过当前活跃密钥
                if (keySpec.isActive()) {
                    continue;
                }
                
                // 检查是否过期
                LocalDateTime activeTime = keySpec.getActiveTime();
                if (activeTime != null && activeTime.isBefore(cutoffTime)) {
                    keyRotationManager.deprecateVersion(version);
                    cleanedCount++;
                    log.info("清理过期密钥版本: {}, 激活时间: {}", version, activeTime);
                }
            }
            
            if (cleanedCount > 0) {
                log.info("清理过期密钥完成，共清理 {} 个版本", cleanedCount);
            } else {
                log.debug("没有需要清理的过期密钥");
            }
            
        } catch (Exception e) {
            log.error("清理过期密钥失败", e);
        }
    }

    /**
     * 发送轮换成功通知
     */
    private void sendRotationNotification(String oldVersion, String newVersion) {
        // 这里可以扩展为发送邮件、消息队列、webhook等
        log.info("密钥轮换通知: {} -> {}", oldVersion, newVersion);
        
        // 示例：记录到审计日志
        recordAuditLog("KEY_ROTATION_SUCCESS", 
            String.format("密钥轮换成功: %s -> %s", oldVersion, newVersion));
    }

    /**
     * 发送轮换失败通知
     */
    private void sendRotationFailureNotification(String currentVersion, String errorMessage) {
        // 这里可以扩展为发送告警邮件、消息等
        log.error("密钥轮换失败通知: 版本={}, 错误={}", currentVersion, errorMessage);
        
        // 示例：记录到审计日志
        recordAuditLog("KEY_ROTATION_FAILURE", 
            String.format("密钥轮换失败: 版本=%s, 错误=%s", currentVersion, errorMessage));
    }

    /**
     * 记录审计日志
     */
    private void recordAuditLog(String action, String details) {
        // 这里可以集成到审计系统
        log.info("审计日志: action={}, details={}, timestamp={}", 
            action, details, LocalDateTime.now());
    }

    /**
     * 手动触发密钥轮换检查（用于测试）
     */
    public void triggerManualCheck() {
        log.info("手动触发密钥轮换检查");
        checkKeyRotation();
    }

    /**
     * 获取下次轮换时间
     */
    public LocalDateTime getNextRotationTime() {
        try {
            KeySpec currentKey = keyRotationManager.getCurrentKeySpec();
            if (currentKey == null || currentKey.getActiveTime() == null) {
                return null;
            }
            
            int rotationDays = properties.getEncryption().getKeyRotation().getAutoRotationDays();
            return currentKey.getActiveTime().plusDays(rotationDays);
            
        } catch (Exception e) {
            log.error("获取下次轮换时间失败", e);
            return null;
        }
    }

    /**
     * 获取轮换状态信息
     */
    public RotationStatus getRotationStatus() {
        try {
            KeySpec currentKey = keyRotationManager.getCurrentKeySpec();
            if (currentKey == null) {
                return RotationStatus.builder()
                    .status("NO_ACTIVE_KEY")
                    .message("未找到活跃密钥")
                    .build();
            }

            LocalDateTime nextRotationTime = getNextRotationTime();
            boolean needsRotation = shouldRotateKey(currentKey);
            
            return RotationStatus.builder()
                .status(needsRotation ? "NEEDS_ROTATION" : "NORMAL")
                .currentVersion(currentKey.getVersion())
                .activeTime(currentKey.getActiveTime())
                .nextRotationTime(nextRotationTime)
                .needsRotation(needsRotation)
                .message(needsRotation ? "密钥需要轮换" : "密钥状态正常")
                .build();
                
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("未设置当前密钥版本")) {
                return RotationStatus.builder()
                    .status("NO_ACTIVE_KEY")
                    .message("未找到活跃密钥")
                    .build();
            }
            log.error("获取轮换状态失败", e);
            return RotationStatus.builder()
                .status("ERROR")
                .message("获取状态失败: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("获取轮换状态失败", e);
            return RotationStatus.builder()
                .status("ERROR")
                .message("获取状态失败: " + e.getMessage())
                .build();
        }
    }

    /**
     * 轮换状态信息
     */
    public static class RotationStatus {
        private String status;
        private String currentVersion;
        private LocalDateTime activeTime;
        private LocalDateTime nextRotationTime;
        private boolean needsRotation;
        private String message;

        public static RotationStatusBuilder builder() {
            return new RotationStatusBuilder();
        }

        // Getters
        public String getStatus() { return status; }
        public String getCurrentVersion() { return currentVersion; }
        public LocalDateTime getActiveTime() { return activeTime; }
        public LocalDateTime getNextRotationTime() { return nextRotationTime; }
        public boolean isNeedsRotation() { return needsRotation; }
        public String getMessage() { return message; }

        public static class RotationStatusBuilder {
            private String status;
            private String currentVersion;
            private LocalDateTime activeTime;
            private LocalDateTime nextRotationTime;
            private boolean needsRotation;
            private String message;

            public RotationStatusBuilder status(String status) { this.status = status; return this; }
            public RotationStatusBuilder currentVersion(String currentVersion) { this.currentVersion = currentVersion; return this; }
            public RotationStatusBuilder activeTime(LocalDateTime activeTime) { this.activeTime = activeTime; return this; }
            public RotationStatusBuilder nextRotationTime(LocalDateTime nextRotationTime) { this.nextRotationTime = nextRotationTime; return this; }
            public RotationStatusBuilder needsRotation(boolean needsRotation) { this.needsRotation = needsRotation; return this; }
            public RotationStatusBuilder message(String message) { this.message = message; return this; }

            public RotationStatus build() {
                RotationStatus status = new RotationStatus();
                status.status = this.status;
                status.currentVersion = this.currentVersion;
                status.activeTime = this.activeTime;
                status.nextRotationTime = this.nextRotationTime;
                status.needsRotation = this.needsRotation;
                status.message = this.message;
                return status;
            }
        }
    }
}
