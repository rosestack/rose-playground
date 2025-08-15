package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.mfa.MfaRegistry;
import io.github.rosestack.spring.boot.security.mfa.MfaService;
import io.github.rosestack.spring.boot.security.mfa.totp.TotpGenerator;
import io.github.rosestack.spring.boot.security.mfa.totp.TotpProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rose.security.mfa.enabled", havingValue = "true")
public class RoseSecurityMfaConfiguration {
    private final RoseSecurityProperties properties;

    // ========== MFA 相关配置 ==========

    @Bean
    @ConditionalOnMissingBean
    public MfaRegistry mfaRegistry() {
        return new MfaRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public MfaService mfaService(MfaRegistry mfaRegistry) {
        return new MfaService(mfaRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public TotpGenerator totpGenerator() {
        RoseSecurityProperties.Mfa.Totp totpConfig = properties.getMfa().getTotp();
        return new TotpGenerator(totpConfig.getTimeStep(), totpConfig.getDigits(), totpConfig.getWindowSize());
    }

    @Bean
    @ConditionalOnMissingBean
    public TotpProvider totpProvider(TotpGenerator totpGenerator, MfaRegistry mfaRegistry) {
        RoseSecurityProperties.Mfa.Totp totpConfig = properties.getMfa().getTotp();
        TotpProvider provider = new TotpProvider(
                totpGenerator,
                totpConfig.getIssuer(),
                totpConfig.getMaxFailureAttempts(),
                totpConfig.getLockoutMinutes());

        // 自动注册到MFA注册表
        mfaRegistry.registerProvider(provider);

        return provider;
    }
}
