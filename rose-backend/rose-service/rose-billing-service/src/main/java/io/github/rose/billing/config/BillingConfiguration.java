package io.github.rose.billing.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;

/**
 * 计费服务配置类
 *
 * @author rose
 */
@Configuration
@EnableScheduling
@EnableTransactionManagement
public class BillingConfiguration {

    /**
     * 计费服务启用配置
     */
    @Bean
    @ConditionalOnProperty(name = "rose.billing.enabled", havingValue = "true", matchIfMissing = true)
    public BillingProperties billingProperties() {
        return new BillingProperties();
    }
}


