package io.github.rosestack.spring.boot.encryption;

import io.github.rosestack.encryption.FieldEncryptor;
import io.github.rosestack.encryption.hash.HashService;
import io.github.rosestack.spring.boot.encryption.config.EncryptorAutoConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rose加密器自动配置测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("Rose加密器自动配置测试")
class EncryptorAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EncryptorAutoConfig.class));

    @Test
    @DisplayName("默认配置下应该创建FieldEncryptor")
    void shouldCreateFieldEncryptorWithDefaultConfig() {
        contextRunner
                .withPropertyValues(
                        "rose.encryption.enabled=true",
                        "rose.encryption.secret-key=MySecretKey12345MySecretKey12345"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(FieldEncryptor.class);
                    assertThat(context.getBean(FieldEncryptor.class)).isNotNull();
                });
    }

    @Test
    @DisplayName("禁用加密时不应该创建Bean")
    void shouldNotCreateBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("rose.encryption.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FieldEncryptor.class);
                    assertThat(context).doesNotHaveBean(HashService.class);
                });
    }

    @Test
    @DisplayName("启用哈希服务时应该创建HashService")
    void shouldCreateHashServiceWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "rose.encryption.enabled=true",
                        "rose.encryption.secret-key=MySecretKey12345MySecretKey12345",
                        "rose.encryption.hash.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(HashService.class);
                    assertThat(context.getBean(HashService.class)).isNotNull();
                });
    }

    @Test
    @DisplayName("禁用哈希服务时不应该创建HashService")
    void shouldNotCreateHashServiceWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "rose.encryption.enabled=true",
                        "rose.encryption.secret-key=MySecretKey12345MySecretKey12345",
                        "rose.encryption.hash.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(HashService.class);
                });
    }

    @Test
    @DisplayName("自定义FieldEncryptor应该优先使用")
    void shouldUseCustomFieldEncryptor() {
        contextRunner
                .withPropertyValues(
                        "rose.encryption.enabled=true",
                        "rose.encryption.secret-key=MySecretKey12345MySecretKey12345"
                )
                .withUserConfiguration(CustomFieldEncryptorConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(FieldEncryptor.class);
                    assertThat(context.getBean(FieldEncryptor.class))
                            .isInstanceOf(CustomFieldEncryptor.class);
                });
    }

    @Test
    @DisplayName("配置属性应该正确绑定")
    void shouldBindConfigurationProperties() {
        contextRunner
                .withPropertyValues(
                        "rose.encryption.enabled=true",
                        "rose.encryption.secret-key=MyTestKey123456789012345678901",
                        "rose.encryption.fail-on-error=false"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(FieldEncryptor.class);
                    // 这里可以进一步验证配置是否正确应用
                });
    }

    /**
     * 自定义FieldEncryptor配置
     */
    @Configuration
    static class CustomFieldEncryptorConfig {

        @Bean
        public FieldEncryptor customFieldEncryptor() {
            return new CustomFieldEncryptor();
        }
    }

    /**
     * 自定义FieldEncryptor实现
     */
    static class CustomFieldEncryptor implements FieldEncryptor {

        @Override
        public String encrypt(String plainText, io.github.rosestack.encryption.enums.EncryptType encryptType) {
            return "custom_encrypted_" + plainText;
        }

        @Override
        public String decrypt(String cipherText, io.github.rosestack.encryption.enums.EncryptType encryptType) {
            return cipherText.replace("custom_encrypted_", "");
        }
    }
}
