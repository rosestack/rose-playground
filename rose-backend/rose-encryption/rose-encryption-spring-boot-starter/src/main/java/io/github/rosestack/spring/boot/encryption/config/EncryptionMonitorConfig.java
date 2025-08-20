package io.github.rosestack.spring.boot.encryption.config;

import io.github.rosestack.spring.boot.encryption.actuator.EncryptionEndpoint;
import io.github.rosestack.spring.boot.encryption.health.EncryptionHealthIndicator;
import io.github.rosestack.spring.boot.encryption.metrics.EncryptionMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 加密服务监控自动配置
 *
 * <p>自动配置加密服务的监控组件，包括：
 * <ul>
 *   <li>健康检查指示器</li>
 *   <li>监控端点</li>
 *   <li>指标收集器</li>
 * </ul>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(after = EncryptorAutoConfig.class)
@EnableConfigurationProperties(EncryptionProperties.class)
@ConditionalOnProperty(prefix = "rose.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EncryptionMonitorConfig {

    /**
     * 健康检查指示器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.boot.actuator.health.HealthIndicator")
    public EncryptionHealthIndicator encryptionHealthIndicator(EncryptionProperties properties) {
        log.info("注册加密服务健康检查指示器");
        return new EncryptionHealthIndicator(properties);
    }

    /**
     * 监控端点
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.boot.actuator.endpoint.annotation.Endpoint")
    public EncryptionEndpoint encryptionEndpoint(EncryptionProperties properties) {
        log.info("注册加密服务监控端点: /actuator/encryption");
        return new EncryptionEndpoint(properties);
    }

    /**
     * 指标收集器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @ConditionalOnProperty(
        prefix = "rose.encryption.metrics",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public EncryptionMetrics encryptionMetrics() {
        log.info("注册加密服务指标收集器");
        return new EncryptionMetrics();
    }
}
