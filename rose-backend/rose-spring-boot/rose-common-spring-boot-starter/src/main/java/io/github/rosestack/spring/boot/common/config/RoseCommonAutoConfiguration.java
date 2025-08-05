package io.github.rosestack.spring.boot.common.config;

import io.github.rosestack.core.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.common.encryption.DefaultFieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.FieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.NoopFieldEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@RequiredArgsConstructor
@PropertySource(value = "classpath:application-rose-common.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.common", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseCommonProperties.class)
@AutoConfiguration
@ComponentScan("io.github.rosestack.spring.boot.common")
public class RoseCommonAutoConfiguration {
    private final RoseCommonProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public FieldEncryptor noopFieldEncryptor() {
        return new NoopFieldEncryptor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "rose.common.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FieldEncryptor fieldEncryptor() {
        return new DefaultFieldEncryptor(properties.getEncryption());
    }
}
