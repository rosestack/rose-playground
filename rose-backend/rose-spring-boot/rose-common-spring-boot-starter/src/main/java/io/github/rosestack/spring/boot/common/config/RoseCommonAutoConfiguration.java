package io.github.rosestack.spring.boot.common.config;

import io.github.rosestack.core.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.common.encryption.FieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.NoopFieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.controller.KeyRotationController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@RequiredArgsConstructor
@PropertySource(value = "classpath:application-rose-common.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.common", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseCommonProperties.class)
@Import({KeyRotationController.class, RoseEncryptorConfiguration.class})
@AutoConfiguration
public class RoseCommonAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public FieldEncryptor noopFieldEncryptor() {
        return new NoopFieldEncryptor();
    }
}
