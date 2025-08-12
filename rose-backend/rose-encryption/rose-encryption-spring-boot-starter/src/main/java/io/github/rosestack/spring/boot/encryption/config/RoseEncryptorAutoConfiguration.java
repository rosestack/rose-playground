package io.github.rosestack.spring.boot.encryption.config;

import io.github.rosestack.encryption.DefaultFieldEncryptor;
import io.github.rosestack.encryption.FieldEncryptor;
import io.github.rosestack.encryption.hash.HashService;
import io.github.rosestack.encryption.rotation.KeyRotationManager;
import io.github.rosestack.encryption.rotation.RotationAwareFieldEncryptor;
import io.github.rosestack.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.encryption.AutoKeyRotationScheduler;
import io.github.rosestack.spring.boot.encryption.controller.KeyRotationController;
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
@PropertySource(value = "classpath:application-rose-encryption.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseEncryptionProperties.class)
@Import({KeyRotationController.class, RoseEncryptorAutoConfiguration.class})
@AutoConfiguration
public class RoseEncryptorAutoConfiguration {
    private final RoseEncryptionProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public FieldEncryptor fieldEncryptor() {
        return new DefaultFieldEncryptor(properties.getSecretKey(), properties.isFailOnError());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.encryption.hash", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HashService hashService() {
        return new HashService(properties.getHashProperties(), properties.isFailOnError());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.encryption.key-rotation", name = "enabled", havingValue = "true")
    public KeyRotationManager keyRotationManager() {
        return new KeyRotationManager();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.encryption.key-rotation", name = "enabled", havingValue = "true")
    public RotationAwareFieldEncryptor rotationAwareFieldEncryptor(KeyRotationManager keyRotationManager) {
        return new RotationAwareFieldEncryptor(keyRotationManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.encryption.key-rotation", name = "enabled", havingValue = "true")
    public AutoKeyRotationScheduler autoKeyRotationScheduler(KeyRotationManager keyRotationManager) {
        return new AutoKeyRotationScheduler(keyRotationManager, properties.getKeyRotationProperties());
    }
}
