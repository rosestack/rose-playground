package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.account.SessionKickoutService;
import io.github.rosestack.spring.boot.security.core.RestAccessDeniedHandler;
import io.github.rosestack.spring.boot.security.core.RestAuthenticationEntryPoint;
import io.github.rosestack.spring.boot.security.core.filter.LoginAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.filter.TokenAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.handler.LoginFailureHandler;
import io.github.rosestack.spring.boot.security.core.handler.LoginSuccessHandler;
import io.github.rosestack.spring.boot.security.core.logout.TokenLogoutHandler;
import io.github.rosestack.spring.boot.security.core.token.OpaqueTokenService;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import io.github.rosestack.spring.boot.security.protect.AccessListFilter;
import io.github.rosestack.spring.boot.security.protect.AccessListMatcher;
import io.github.rosestack.spring.boot.security.protect.AccessListStore;
import io.github.rosestack.spring.boot.security.protect.MemoryAccessListStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(RoseSecurityProperties.class)
@Import(AuthenticationConfiguration.class)
@ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoseSecurityAutoConfiguration {

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RoseSecurityProperties props,
            RestAuthenticationEntryPoint entryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            TokenService tokenService,
            AuthenticationManager authenticationManager,
            @Autowired(required = false) LoginLockoutService lockoutService,
            @Autowired(required = false) AccessListStore accessListStore)
            throws Exception {
        // 基础放行路径
        List<String> permit = props.getPermitAll();

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> {
                    if (permit != null && !permit.isEmpty()) {
                        reg.requestMatchers(permit.toArray(new String[0])).permitAll();
                    }
                    reg.requestMatchers(props.getLoginPath(), props.getLogoutPath())
                            .permitAll();
                    reg.requestMatchers(props.getBasePath()).authenticated();
                    reg.anyRequest().permitAll();
                })
                .exceptionHandling(
                        ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl(props.getLogoutPath())
                        .addLogoutHandler(new TokenLogoutHandler(tokenService, props)));
        // Filters
        http.addFilterBefore(
                new LoginAuthenticationFilter(
                        authenticationManager,
                        props,
                        new LoginSuccessHandler(tokenService, lockoutService),
                        new LoginFailureHandler(lockoutService)),
                UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(
                new TokenAuthenticationFilter(tokenService, props), UsernamePasswordAuthenticationFilter.class);

        // Access list filter placed after TokenAuthenticationFilter to get username
        if (accessListStore != null) {
            AccessListMatcher matcher = new AccessListMatcher(accessListStore(props), props);
            http.addFilterAfter(new AccessListFilter(matcher, props), UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(TokenService.class)
    @ConditionalOnProperty(prefix = "rose.security.token", name = "type", havingValue = "LOCAL", matchIfMissing = true)
    public TokenService opaqueTokenService(RoseSecurityProperties props) {
        return new OpaqueTokenService(props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.account.loginLock",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public LoginLockoutService loginLockoutService(RoseSecurityProperties props) {
        return new LoginLockoutService(props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.account.kickout",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public SessionKickoutService sessionKickoutService(TokenService tokenService, RoseSecurityProperties props) {
        return new SessionKickoutService(tokenService, props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.protect.access-list",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public AccessListStore accessListStore(RoseSecurityProperties props) {
        return new MemoryAccessListStore();
    }
}