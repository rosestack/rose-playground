package io.github.rosestack.spring.boot.encryption.health;

import io.github.rosestack.encryption.EncryptionUtils;
import io.github.rosestack.encryption.enums.EncryptType;
import io.github.rosestack.encryption.monitor.EncryptionMonitorManager;
import io.github.rosestack.spring.boot.encryption.config.EncryptionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 加密服务健康检查指示器
 *
 * <p>检查加密服务的健康状态，包括：
 * <ul>
 *   <li>基础加密解密功能是否正常</li>
 *   <li>密钥配置是否有效</li>
 *   <li>性能统计信息</li>
 *   <li>错误率统计</li>
 * </ul>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(HealthIndicator.class)
public class EncryptionHealthIndicator implements HealthIndicator {

    private final EncryptionProperties properties;

    private static final String TEST_PLAIN_TEXT = "health-check-test";
    private static final long HEALTH_CHECK_TIMEOUT_MS = 5000; // 5秒超时

    @Override
    public Health health() {
        try {
            return performHealthCheck();
        } catch (Exception e) {
            log.error("加密服务健康检查失败", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        }
    }

    /**
     * 执行健康检查
     */
    private Health performHealthCheck() {
        Health.Builder builder = Health.up();

        // 1. 检查配置
        checkConfiguration(builder);

        // 2. 检查基础功能
        checkBasicFunctionality(builder);

        // 3. 添加性能统计
        addPerformanceMetrics(builder);

        // 4. 添加系统信息
        addSystemInfo(builder);

        return builder.build();
    }

    /**
     * 检查配置
     */
    private void checkConfiguration(Health.Builder builder) {
        builder.withDetail("enabled", properties.isEnabled());

        if (!properties.isEnabled()) {
            builder.withDetail("status", "DISABLED");
            return;
        }

        // 检查密钥配置
        String secretKey = properties.getSecretKey();
        if (secretKey == null || secretKey.trim().isEmpty()) {
            builder.down().withDetail("error", "密钥未配置");
            return;
        }

        if (secretKey.length() < 16) {
            builder.down().withDetail("error", "密钥长度不足，至少需要16个字符");
            return;
        }

        builder.withDetail("secretKeyLength", secretKey.length());
        builder.withDetail("failOnError", properties.isFailOnError());
    }

    /**
     * 检查基础功能
     */
    private void checkBasicFunctionality(Health.Builder builder) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            // 测试 AES 加密解密
            String encrypted = EncryptionUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, properties.getSecretKey());
            String decrypted = EncryptionUtils.decrypt(encrypted, EncryptType.AES, properties.getSecretKey());

            long duration = System.currentTimeMillis() - startTime;

            if (!TEST_PLAIN_TEXT.equals(decrypted)) {
                builder.down().withDetail("error", "加密解密结果不匹配");
                return;
            }

            if (duration > HEALTH_CHECK_TIMEOUT_MS) {
                builder.down().withDetail("error", "加密解密操作超时");
                return;
            }

            builder.withDetail("basicFunctionality", "OK");
            builder.withDetail("testDuration", duration + "ms");

        } catch (Exception e) {
            builder.down().withDetail("basicFunctionalityError", e.getMessage());
        }
    }

    /**
     * 添加性能统计
     */
    private void addPerformanceMetrics(Health.Builder builder) {
        try {
            Map<String, Object> report = EncryptionMonitorManager.getInstance().getMonitoringReport();

            // 基础统计
            @SuppressWarnings("unchecked")
            Map<String, Object> systemInfo = (Map<String, Object>) report.get("systemInfo");
            if (systemInfo != null) {
                builder.withDetail("totalOperations", systemInfo.get("totalOperations"));
            }

            // 成功率
            @SuppressWarnings("unchecked")
            Map<String, Double> successRates = (Map<String, Double>) report.get("successRates");
            if (successRates != null && !successRates.isEmpty()) {
                builder.withDetail("successRates", successRates);

                // 检查成功率是否过低
                double avgSuccessRate = successRates.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(100.0);

                if (avgSuccessRate < 95.0) {
                    builder.down().withDetail("warning", "成功率过低: " + String.format("%.1f%%", avgSuccessRate));
                }
            }

            // 错误统计
            @SuppressWarnings("unchecked")
            Map<String, Long> errors = (Map<String, Long>) report.get("errors");
            if (errors != null && !errors.isEmpty()) {
                long totalErrors = errors.getOrDefault("encrypt_total_errors", 0L) +
                                 errors.getOrDefault("decrypt_total_errors", 0L);
                builder.withDetail("totalErrors", totalErrors);

                if (totalErrors > 100) {
                    builder.down().withDetail("warning", "错误数量过多: " + totalErrors);
                }
            }

        } catch (Exception e) {
            log.warn("获取性能统计失败", e);
            builder.withDetail("metricsError", e.getMessage());
        }
    }

    /**
     * 添加系统信息
     */
    private void addSystemInfo(Health.Builder builder) {
        builder.withDetail("version", getClass().getPackage().getImplementationVersion());
        builder.withDetail("timestamp", System.currentTimeMillis());

        // 缓存统计
        try {
            String cacheStats = EncryptionMonitorManager.getInstance().getCacheStats();
            builder.withDetail("cacheStats", cacheStats);
        } catch (Exception e) {
            log.warn("获取缓存统计失败", e);
        }

        // JVM 信息
        Runtime runtime = Runtime.getRuntime();
        builder.withDetail("jvm", Map.of(
            "maxMemory", runtime.maxMemory() / 1024 / 1024 + "MB",
            "totalMemory", runtime.totalMemory() / 1024 / 1024 + "MB",
            "freeMemory", runtime.freeMemory() / 1024 / 1024 + "MB",
            "processors", runtime.availableProcessors()
        ));
    }
}
