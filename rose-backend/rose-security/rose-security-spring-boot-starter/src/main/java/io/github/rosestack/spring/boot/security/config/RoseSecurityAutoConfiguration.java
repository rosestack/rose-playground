package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.account.TokenKickoutService;
import io.github.rosestack.spring.boot.security.core.RestAccessDeniedHandler;
import io.github.rosestack.spring.boot.security.core.RestAuthenticationEntryPoint;
import io.github.rosestack.spring.boot.security.core.filter.LoginAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.filter.LoginPreCheckFilter;
import io.github.rosestack.spring.boot.security.core.filter.TokenAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.handler.LoginFailureHandler;
import io.github.rosestack.spring.boot.security.core.handler.LoginSuccessHandler;
import io.github.rosestack.spring.boot.security.core.handler.LogoutSuccessHandler;
import io.github.rosestack.spring.boot.security.core.token.OpaqueTokenService;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import io.github.rosestack.spring.boot.security.protect.AccessListFilter;
import io.github.rosestack.spring.boot.security.protect.RateLimitFilter;
import io.github.rosestack.spring.boot.security.protect.ReplayFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(RoseSecurityProperties.class)
@Import({
    RoseSecurityAutoConfiguration.RoseAuthenticationConfiguration.class,
    RoseAccountConfiguration.class,
    RoseProtectConfiguration.class
})
@ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoseSecurityAutoConfiguration {
    private static final String[] PUBLIC_RESOURCES = {
        "/favicon.ico", "/actuator/**", "/error",
    };

    private final RoseSecurityProperties props;
    private final ObjectProvider<AccessListFilter> accessListFilterProvider;
    private final ObjectProvider<ReplayFilter> replayFilterObjectProvider;
    private final ObjectProvider<RateLimitFilter> rateLimitFilterObjectProvider;
    private final ObjectProvider<LoginPreCheckFilter> loginPreCheckFilterObjectProvider;

    private final AuthenticationManager authenticationManager;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final TokenService tokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler accessDeniedHandler)
            throws Exception {

        http.csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.securityMatcher(props.getBasePath()).authorizeHttpRequests(reg -> reg.requestMatchers(PUBLIC_RESOURCES)
                .permitAll()
                .requestMatchers(props.getPermitAll())
                .permitAll()
                .requestMatchers(props.getLoginPath(), props.getLogoutPath())
                .permitAll()
                .anyRequest()
                .authenticated());

        http.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler));

        http.logout(logout -> logout.logoutUrl(props.getLogoutPath()).addLogoutHandler(logoutSuccessHandler));

        // Login pre-check (account locked)
        if (loginPreCheckFilterObjectProvider.getIfAvailable() != null) {
            http.addFilterBefore(
                    loginPreCheckFilterObjectProvider.getIfAvailable(), UsernamePasswordAuthenticationFilter.class);
        }

        http.addFilterBefore(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Access list filter placed after TokenAuthenticationFilter to get username
        if (accessListFilterProvider.getIfAvailable() != null) {
            http.addFilterAfter(accessListFilterProvider.getIfAvailable(), UsernamePasswordAuthenticationFilter.class);
        }

        // Replay protection (before rate limit)
        if (replayFilterObjectProvider.getIfAvailable() != null) {
            http.addFilterAfter(
                    replayFilterObjectProvider.getIfAvailable(), UsernamePasswordAuthenticationFilter.class);
        }
        // Rate limiting
        if (rateLimitFilterObjectProvider.getIfAvailable() != null) {
            http.addFilterAfter(
                    rateLimitFilterObjectProvider.getIfAvailable(), UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter() {
        return new LoginAuthenticationFilter(authenticationManager, props, loginSuccessHandler, loginFailureHandler);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenService, props);
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }

    @RequiredArgsConstructor
    public static class RoseAuthenticationConfiguration {
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
        @ConditionalOnProperty(
                prefix = "rose.security.token",
                name = "type",
                havingValue = "LOCAL",
                matchIfMissing = true)
        public TokenService opaqueTokenService(RoseSecurityProperties props) {
            return new OpaqueTokenService(props);
        }
    }
}