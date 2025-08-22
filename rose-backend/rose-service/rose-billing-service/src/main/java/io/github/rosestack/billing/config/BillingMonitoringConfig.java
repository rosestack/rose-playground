package io.github.rosestack.billing.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 计费服务监控配置类
 * <p>
 * 配置计费服务的监控指标和度量
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Configuration
public class BillingMonitoringConfig {

    /**
     * 自定义MeterRegistry配置
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "billing-service");
    }

    /**
     * 计费计算耗时监控
     */
    @Bean
    Timer billingCalculationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("billing.calculation.duration")
                .description("计费计算耗时")
                .register(meterRegistry);
    }

    /**
     * 配额检查耗时监控
     */
    @Bean
    Timer quotaCheckTimer(MeterRegistry meterRegistry) {
        return Timer.builder("billing.quota.check.duration")
                .description("配额检查耗时")
                .register(meterRegistry);
    }

    /**
     * Outbox事件处理耗时监控
     */
    @Bean
    Timer outboxEventProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("billing.outbox.processing.duration")
                .description("Outbox事件处理耗时")
                .register(meterRegistry);
    }

    /**
     * 订阅创建耗时监控
     */
    @Bean
    Timer subscriptionCreationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("billing.subscription.creation.duration")
                .description("订阅创建耗时")
                .register(meterRegistry);
    }

    /**
     * 账单生成耗时监控
     */
    @Bean
    Timer invoiceGenerationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("billing.invoice.generation.duration")
                .description("账单生成耗时")
                .register(meterRegistry);
    }
}