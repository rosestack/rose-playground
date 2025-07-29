package io.github.rosestack.billing.config;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 计费系统自动配置类
 * <p>
 * 使用纯注解方式，不依赖 XML 映射文件
 *
 * @author rose
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(BillingProperties.class)
@ConditionalOnProperty(prefix = "rose.billing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BillingAutoConfiguration {

}
