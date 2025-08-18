package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.account.TokenKickoutService;
import io.github.rosestack.spring.boot.security.core.handler.LoginFailureHandler;
import io.github.rosestack.spring.boot.security.core.handler.LoginSuccessHandler;
import io.github.rosestack.spring.boot.security.core.handler.LogoutSuccessHandler;
import io.github.rosestack.spring.boot.security.core.token.OpaqueTokenService;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class RoseAuthenticationConfiguration {
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<LoginLockoutService> loginLockoutServiceProvider;
    private final ObjectProvider<TokenKickoutService> tokenKickoutServiceProvider;

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
    public LoginSuccessHandler loginSuccessHandler(TokenService tokenService) {
        return new LoginSuccessHandler(
                tokenService, loginLockoutServiceProvider, tokenKickoutServiceProvider, eventPublisher);
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(TokenService tokenService, RoseSecurityProperties props) {
        return new LogoutSuccessHandler(tokenService, props, eventPublisher);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler(loginLockoutServiceProvider);
    }

    @Bean
    @ConditionalOnMissingBean(TokenService.class)
    @ConditionalOnProperty(prefix = "rose.security.token", name = "type", havingValue = "LOCAL", matchIfMissing = true)
    public TokenService opaqueTokenService(RoseSecurityProperties props) {
        return new OpaqueTokenService(props);
    }
}
