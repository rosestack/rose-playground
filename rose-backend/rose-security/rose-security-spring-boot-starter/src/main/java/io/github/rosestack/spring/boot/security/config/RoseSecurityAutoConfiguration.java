package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.security.auth.controller.AuthController;
import io.github.rosestack.spring.boot.security.auth.filter.TokenAuthenticationFilter;
import io.github.rosestack.spring.boot.security.auth.service.TokenService;
import io.github.rosestack.spring.boot.security.auth.service.impl.MemoryTokenService;
import io.github.rosestack.spring.boot.security.auth.service.impl.RedisTokenService;
import io.github.rosestack.spring.boot.security.extension.AuditEventPublisher;
import io.github.rosestack.spring.boot.security.extension.AuthenticationHook;
import io.github.rosestack.spring.boot.security.extension.DefaultAuthenticationHook;
import io.github.rosestack.spring.boot.security.extension.LoggingAuditEventPublisher;
import io.github.rosestack.spring.boot.security.properties.RoseSecurityProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Rose Security 自动配置类
 *
 * <p>提供完整的认证与授权功能的自动配置，包括：
 * - 基础认证模块
 * - 会话管理模块
 * - JWT 模块
 * - OAuth2 模块
 * - MFA 模块
 * - 安全防护模块
 * - 可观测性模块
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = SecurityAutoConfiguration.class)
@PropertySource(value = "classpath:application-rose-security.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseSecurityProperties.class)
@EnableWebSecurity
public class RoseSecurityAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("Rose Security 自动配置已启用");
    }

    @Bean
    @ConditionalOnMissingBean(TokenService.class)
    @ConditionalOnProperty(prefix = "rose.security.auth.token", name = "storageType", havingValue = "memory")
    public TokenService tokenService(RoseSecurityProperties properties) {
        return new MemoryTokenService(properties);
    }

    @Bean
    @ConditionalOnMissingBean(TokenService.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "rose.security.auth.token", name = "storageType", havingValue = "redis")
    public TokenService tokenService(RedisTemplate redisTemplate, RoseSecurityProperties properties) {
        return new RedisTokenService(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.security.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TokenAuthenticationFilter tokenAuthenticationFilter(
            TokenService tokenService, RoseSecurityProperties properties) {
        return new TokenAuthenticationFilter(tokenService, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnBean(UserDetailsService.class)
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(AuthController.class)
    public AuthController authController(TokenService tokenService,
                                         AuthenticationManager authenticationManager,
                                         AuthenticationHook authenticationHook,
                                         AuditEventPublisher auditEventPublisher) {
        return new AuthController(tokenService, authenticationManager, authenticationHook, auditEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationHook.class)
    public AuthenticationHook authenticationHook() {
        return new DefaultAuthenticationHook();
    }

    @Bean
    @ConditionalOnMissingBean(AuditEventPublisher.class)
    public AuditEventPublisher auditEventPublisher() {
        return new LoggingAuditEventPublisher();
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, RoseSecurityProperties properties, TokenAuthenticationFilter tokenAuthenticationFilter)
            throws Exception {
        String loginPath = properties.getAuth().getLoginPath();
        String logoutPath = properties.getAuth().getLogoutPath();
        String refreshPath = properties.getAuth().getRefreshPath();
        boolean stateless = properties.isStateless();

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        stateless ? SessionCreationPolicy.STATELESS : SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(loginPath, logoutPath, refreshPath)
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
