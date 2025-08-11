package io.github.rosestack.spring.boot.common.config;

import io.github.rosestack.spring.boot.common.encryption.DefaultFieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.FieldEncryptor;
import io.github.rosestack.spring.boot.common.encryption.hash.HashService;
import io.github.rosestack.spring.boot.common.encryption.rotation.AutoKeyRotationScheduler;
import io.github.rosestack.spring.boot.common.encryption.rotation.KeyRotationManager;
import io.github.rosestack.spring.boot.common.encryption.rotation.RotationAwareFieldEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.common.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoseEncryptorConfiguration {
    private final RoseCommonProperties properties;

    @Bean
    public FieldEncryptor fieldEncryptor() {
        return new DefaultFieldEncryptor(properties.getEncryption());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.common.encryption.hash", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HashService hashService() {
        return new HashService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.common.encryption.key-rotation", name = "enabled", havingValue = "true")
    public KeyRotationManager keyRotationManager() {
        return new KeyRotationManager();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.common.encryption.key-rotation", name = "enabled", havingValue = "true")
    public RotationAwareFieldEncryptor rotationAwareFieldEncryptor(KeyRotationManager keyRotationManager) {
        return new RotationAwareFieldEncryptor(keyRotationManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.common.encryption.key-rotation", name = "enabled", havingValue = "true")
    public AutoKeyRotationScheduler autoKeyRotationScheduler(KeyRotationManager keyRotationManager) {
        return new AutoKeyRotationScheduler(keyRotationManager, properties);
    }
}