package io.github.rosestack.auth.config;

import io.github.rosestack.auth.properties.AuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 认证模块自动配置类
 * <p>
 * 提供认证模块的自动配置功能，包括 JWT、OAuth2、安全策略等配置。
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "rose.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuthProperties.class)
@Import({
    JwtConfiguration.class,
    OAuth2ClientConfiguration.class,
    SecurityConfiguration.class
})
public class AuthAutoConfiguration {
    
    public AuthAutoConfiguration() {
        log.info("Rose Auth Spring Boot Starter 已启用");
    }
}
