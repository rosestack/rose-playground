package io.github.rosestack.spring.boot.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置类
 *
 * <p>提供 JWT 令牌管理相关的配置和 Bean 定义。
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "rose.auth.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class JwtConfiguration {

    private final AuthProperties authProperties;

    // TODO: 添加 JWT 相关的 Bean 配置
    // - JwtTokenProvider
    // - JwtAuthenticationFilter
    // - JwtTokenStore
    // - JwtDecoder/JwtEncoder
}
