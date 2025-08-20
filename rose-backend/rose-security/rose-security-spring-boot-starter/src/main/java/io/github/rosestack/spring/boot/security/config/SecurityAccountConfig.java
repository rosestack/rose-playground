package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.account.TokenKickoutService;
import io.github.rosestack.spring.boot.security.core.filter.LoginPreCheckFilter;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "rose.security.account", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityAccountConfig {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.account.loginLock",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public LoginLockoutService loginLockoutService(SecurityProperties props) {
        return new LoginLockoutService(props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.account.loginLock",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public LoginPreCheckFilter loginPreCheckFilter(
		SecurityProperties props, ObjectProvider<LoginLockoutService> loginLockoutServiceProvider) {
        return new LoginPreCheckFilter(props, loginLockoutServiceProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.account.kickout",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public TokenKickoutService tokenKickoutService(TokenService tokenService, SecurityProperties props) {
        return new TokenKickoutService(tokenService, props);
    }
}
