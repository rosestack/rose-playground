package io.github.rosestack.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Web 自动配置
 * <p>
 * 提供 Web 相关的自动配置功能
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@EnableAspectJAutoProxy(proxyTargetClass = true)
@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
@ConditionalOnProperty(prefix = "rose.web", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
        CorsConfig.class,
        JacksonConfig.class,
        AsyncConfig.class,
        MessageConfig.class
})
public class WebAutoConfig {

} 