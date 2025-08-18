package io.github.rosestack.spring.boot.security.config;

import io.github.rosestack.spring.boot.security.protect.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "rose.security.protect", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoseProtectConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.protect.access-list",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public AccessListStore accessListStore() {
        return new MemoryAccessListStore();
    }

    @Bean
    AccessListFilter accessListFilter(
            RoseSecurityProperties props, ObjectProvider<AccessListStore> accessListStoreProvider) {
        AccessListMatcher matcher = new AccessListMatcher(accessListStoreProvider, props);

        return new AccessListFilter(matcher, props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.protect.replay",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ReplayFilter replayFilter(RoseSecurityProperties props) {
        return new ReplayFilter(new ReplayProtection(props), props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "rose.security.protect.rate-limit",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RateLimitFilter rateLimitFilter(RoseSecurityProperties props) {
        return new RateLimitFilter(new RateLimiter(props), props);
    }
}