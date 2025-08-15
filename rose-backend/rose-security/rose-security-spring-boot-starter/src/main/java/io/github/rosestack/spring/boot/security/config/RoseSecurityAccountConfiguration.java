package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.account.CaptchaService;
import io.github.rosestack.spring.boot.security.account.LoginAttemptService;
import io.github.rosestack.spring.boot.security.account.PasswordChangeService;
import io.github.rosestack.spring.boot.security.account.PasswordPolicyService;
import io.github.rosestack.spring.boot.security.account.impl.DefaultPasswordChangeService;
import io.github.rosestack.spring.boot.security.account.impl.DefaultPasswordPolicyService;
import io.github.rosestack.spring.boot.security.account.impl.InMemoryLoginAttemptService;
import io.github.rosestack.spring.boot.security.account.impl.NoopCaptchaService;
import io.github.rosestack.spring.boot.security.mfa.MfaRegistry;
import io.github.rosestack.spring.boot.security.mfa.MfaService;
import io.github.rosestack.spring.boot.security.mfa.totp.TotpGenerator;
import io.github.rosestack.spring.boot.security.mfa.totp.TotpProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rose.security.account.enabled", havingValue = "true")
public class RoseSecurityAccountConfiguration {
    private final RoseSecurityProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public CaptchaService captchaService() {
        return new NoopCaptchaService();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginAttemptService loginAttemptService() {
        return new InMemoryLoginAttemptService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordPolicyService passwordPolicyService(PasswordEncoder passwordEncoder) {
        return new DefaultPasswordPolicyService(properties.getAccount().getPassword(), passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordChangeService passwordChangeService(
            PasswordPolicyService passwordPolicyService,
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {
        return new DefaultPasswordChangeService(passwordPolicyService, passwordEncoder, userDetailsService);
    }
}
