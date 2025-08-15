package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.security.account.CaptchaService;
import io.github.rosestack.spring.boot.security.account.LoginAttemptService;
import io.github.rosestack.spring.boot.security.core.controller.AuthController;
import io.github.rosestack.spring.boot.security.core.filter.TokenAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.service.LoginService;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.service.impl.MemoryTokenService;
import io.github.rosestack.spring.boot.security.core.service.impl.RedisTokenService;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationLifecycleHook;
import io.github.rosestack.spring.boot.security.jwt.TokenManagementHook;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
@RequiredArgsConstructor
public class RoseSecurityAutoConfiguration {
    private final RoseSecurityProperties properties;

    @PostConstruct
    public void init() {
        log.info("Initializing Rose Security Module");
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.security.token", name = "storageType", havingValue = "memory")
    public TokenService memoryTokenService(TokenManagementHook authenticationHook) {
        return new MemoryTokenService(properties.getToken(), authenticationHook);
    }

    @Bean
    @ConditionalOnMissingBean(name = "jwtTokenService")
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(prefix = "rose.security.token", name = "storageType", havingValue = "redis")
    public TokenService redisTokenService(
            RedisTemplate<String, Object> redisTemplate, TokenManagementHook authenticationHook) {
        return new RedisTokenService(properties.getToken(), authenticationHook, redisTemplate);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(TokenService tokenService) {
        return new TokenAuthenticationFilter(tokenService, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    public AuthController authController(LoginService loginService, TokenService tokenService) {
        return new AuthController(loginService, tokenService);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginService loginService(
            TokenService tokenService,
            AuthenticationManager authenticationManager,
            AuthenticationLifecycleHook authenticationHook,
            @Autowired(required = false) LoginAttemptService loginAttemptService,
            @Autowired(required = false) CaptchaService captchaService) {
        return new LoginService(
                tokenService, authenticationManager, authenticationHook, loginAttemptService, captchaService);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenManagementHook authenticationHook() {
        return new TokenManagementHook() {};
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationLifecycleHook authenticationLifecycleHook() {
        return new AuthenticationLifecycleHook() {};
    }

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {
        String loginPath = properties.getLoginPath();
        String logoutPath = properties.getLogoutPath();
        String refreshPath = properties.getRefreshPath();
        String basePath = properties.getBashPath();
        String[] permitPaths = properties.getPermitPaths();

        List<String> permits = new ArrayList<>();
        Collections.addAll(permits, permitPaths);
        permits.add(loginPath);
        permits.add(logoutPath);
        permits.add(refreshPath);

        http.securityMatcher(basePath) // 限制只匹配 API 路径
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(permits.toArray(new String[0]))
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
