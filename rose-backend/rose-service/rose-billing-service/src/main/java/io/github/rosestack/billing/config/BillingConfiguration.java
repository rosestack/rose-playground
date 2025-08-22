package io.github.rosestack.billing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 计费系统配置类
 *
 * 配置计费系统的相关Bean和特性
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Configuration
@EnableScheduling
public class BillingConfiguration {

    // 可以在这里添加其他配置，比如：
    // - 计费引擎配置
    // - 支付网关配置
    // - 缓存配置
    // - 消息队列配置等
}