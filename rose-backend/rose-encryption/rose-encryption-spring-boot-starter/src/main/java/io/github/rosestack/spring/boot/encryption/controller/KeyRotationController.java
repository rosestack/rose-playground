package io.github.rosestack.spring.boot.encryption.controller;

import io.github.rosestack.encryption.enums.EncryptType;
import io.github.rosestack.encryption.rotation.KeyRotationManager;
import io.github.rosestack.encryption.rotation.KeySpec;
import io.github.rosestack.spring.boot.encryption.AutoKeyRotationScheduler;
import io.github.rosestack.spring.boot.encryption.config.RoseEncryptionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 密钥轮换管理接口
 *
 * <p>提供密钥轮换的管理功能，包括手动轮换、查看密钥状态等
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/encryption/key-rotation")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.encryption.key-rotation", name = "enabled", havingValue = "true")
public class KeyRotationController {

    private final KeyRotationManager keyRotationManager;
    private final RoseEncryptionProperties properties;
    private final AutoKeyRotationScheduler autoRotationScheduler;

    /**
     * 获取所有密钥版本信息
     */
    @GetMapping("/keys")
    public Map<String, Object> getAllKeys() {
        Map<String, Object> result = new HashMap<>();
        result.put("keys", keyRotationManager.getAllKeySpecs());
        result.put("currentVersion", keyRotationManager.getCurrentKeySpec().getVersion());
        return result;
    }

    /**
     * 获取当前活跃密钥信息
     */
    @GetMapping("/current")
    public KeySpec getCurrentKey() {
        return keyRotationManager.getCurrentKeySpec();
    }

    /**
     * 手动轮换密钥
     */
    @PostMapping("/rotate")
    public Map<String, Object> rotateKey(@RequestBody RotateKeyRequest request) {
        try {
            String oldVersion = keyRotationManager.getCurrentKeySpec().getVersion();
            String newVersion = keyRotationManager.rotateToNewVersion(request.getEncryptType());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("oldVersion", oldVersion);
            result.put("newVersion", newVersion);
            result.put("message", "密钥轮换成功");

            log.info("手动密钥轮换: {} -> {}", oldVersion, newVersion);
            return result;

        } catch (Exception e) {
            log.error("密钥轮换失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密钥轮换失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 切换到指定版本
     */
    @PostMapping("/switch/{version}")
    public Map<String, Object> switchToVersion(@PathVariable String version) {
        try {
            String oldVersion = keyRotationManager.getCurrentKeySpec().getVersion();
            keyRotationManager.setCurrentVersion(version);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("oldVersion", oldVersion);
            result.put("newVersion", version);
            result.put("message", "密钥版本切换成功");

            log.info("密钥版本切换: {} -> {}", oldVersion, version);
            return result;

        } catch (Exception e) {
            log.error("密钥版本切换失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密钥版本切换失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 废弃指定版本
     */
    @PostMapping("/deprecate/{version}")
    public Map<String, Object> deprecateVersion(@PathVariable String version) {
        try {
            keyRotationManager.deprecateVersion(version);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "密钥版本已废弃: " + version);

            log.info("废弃密钥版本: {}", version);
            return result;

        } catch (Exception e) {
            log.error("废弃密钥版本失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "废弃密钥版本失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 清理过期密钥
     */
    @PostMapping("/cleanup")
    public Map<String, Object> cleanupExpiredKeys() {
        try {
            keyRotationManager.cleanupExpiredKeys();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "过期密钥清理完成");

            log.info("手动清理过期密钥");
            return result;

        } catch (Exception e) {
            log.error("清理过期密钥失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "清理过期密钥失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取自动轮换状态
     */
    @GetMapping("/auto-rotation/status")
    public AutoKeyRotationScheduler.RotationStatus getAutoRotationStatus() {
        return autoRotationScheduler.getRotationStatus();
    }

    /**
     * 手动触发轮换检查
     */
    @PostMapping("/auto-rotation/check")
    public Map<String, Object> triggerRotationCheck() {
        try {
            autoRotationScheduler.triggerManualCheck();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "轮换检查已触发");

            return result;

        } catch (Exception e) {
            log.error("触发轮换检查失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "触发轮换检查失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取下次轮换时间
     */
    @GetMapping("/auto-rotation/next-time")
    public Map<String, Object> getNextRotationTime() {
        Map<String, Object> result = new HashMap<>();
        try {
            java.time.LocalDateTime nextTime = autoRotationScheduler.getNextRotationTime();
            result.put("success", true);
            result.put("nextRotationTime", nextTime);
            result.put("message", nextTime != null ? "下次轮换时间: " + nextTime : "未设置轮换时间");

        } catch (Exception e) {
            log.error("获取下次轮换时间失败", e);
            result.put("success", false);
            result.put("message", "获取下次轮换时间失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 密钥轮换请求
     */
    public static class RotateKeyRequest {
        private EncryptType encryptType = EncryptType.AES;

        public EncryptType getEncryptType() {
            return encryptType;
        }

        public void setEncryptType(EncryptType encryptType) {
            this.encryptType = encryptType;
        }
    }
}
