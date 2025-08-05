package io.github.rosestack.spring.boot.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 客户端配置类
 * <p>
 * 提供 OAuth2 客户端相关的配置和 Bean 定义。
 * 
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "rose.auth.oauth2", name = "enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class OAuth2ClientConfiguration {
    
    private final AuthProperties authProperties;
    
    // TODO: 添加 OAuth2 客户端相关的 Bean 配置
    // - ClientRegistrationRepository
    // - OAuth2AuthorizedClientService
    // - OAuth2LoginSuccessHandler
    // - OAuth2UserService
}
