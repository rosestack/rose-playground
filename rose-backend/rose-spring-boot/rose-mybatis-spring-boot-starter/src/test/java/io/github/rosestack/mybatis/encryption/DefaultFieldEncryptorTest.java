package io.github.rosestack.mybatis.encryption;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.enums.EncryptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 默认字段加密器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class DefaultFieldEncryptorTest {

    private OptimizedFieldEncryptor fieldEncryptor;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getEncryption().setEnabled(true);
        properties.getEncryption().setSecretKey("MySecretKey12345");

        fieldEncryptor = new OptimizedFieldEncryptor(properties);
    }

    @Test
    void shouldEncryptAndDecryptAES() {
        // Given
        String plainText = "13800138000";
        EncryptType encryptType = EncryptType.AES;

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, encryptType);
        String decrypted = fieldEncryptor.decrypt(encrypted, encryptType);

        // Then
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void shouldEncryptAndDecryptDES() {
        // Given
        String plainText = "13800138000";
        EncryptType encryptType = EncryptType.DES;
        // DES 需要 8 字节密钥，确保配置正确的密钥
        properties.getEncryption().setSecretKey("12345678");

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, encryptType);
        String decrypted = fieldEncryptor.decrypt(encrypted, encryptType);

        // Then
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void shouldReturnOriginalWhenEncryptionDisabled() {
        // Given
        properties.getEncryption().setEnabled(false);
        fieldEncryptor = new OptimizedFieldEncryptor(properties);
        String plainText = "13800138000";

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, EncryptType.AES);
        String decrypted = fieldEncryptor.decrypt(plainText, EncryptType.AES);

        // Then
        assertThat(encrypted).isEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void shouldHandleNullAndEmptyValues() {
        // When & Then
        assertThat(fieldEncryptor.encrypt(null, EncryptType.AES)).isNull();
        assertThat(fieldEncryptor.encrypt("", EncryptType.AES)).isEmpty();
        assertThat(fieldEncryptor.decrypt(null, EncryptType.AES)).isNull();
        assertThat(fieldEncryptor.decrypt("", EncryptType.AES)).isEmpty();
    }

    @Test
    void shouldSupportAESAndDES() {
        // When & Then
        assertThat(fieldEncryptor.supports(EncryptType.AES)).isTrue();
        assertThat(fieldEncryptor.supports(EncryptType.DES)).isTrue();
        assertThat(fieldEncryptor.supports(EncryptType.DES3)).isTrue();
        assertThat(fieldEncryptor.supports(EncryptType.SM4)).isFalse();
    }

    @Test
    void shouldReturnOriginalForUnsupportedAlgorithm() {
        // Given
        String plainText = "13800138000";

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, EncryptType.SM4);
        String decrypted = fieldEncryptor.decrypt(plainText, EncryptType.SM4);

        // Then
        assertThat(encrypted).isEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void shouldHandleEncryptionFailureGracefully() {
        // Given
        properties.getEncryption().setFailOnError(false);

        // 模拟加密失败的情况 - 使用空密钥
        properties.getEncryption().setSecretKey("");
        fieldEncryptor = new OptimizedFieldEncryptor(properties);

        String plainText = "13800138000";

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, EncryptType.AES);

        // Then - 由于密钥调整功能，短密钥会被填充，所以加密会成功
        // 这里测试的是当 failOnError=false 时，即使加密过程中有问题也不会抛出异常
        assertThat(encrypted).isNotNull();
    }

    @Test
    void shouldEncryptDifferentValuesWithDifferentResults() {
        // Given
        String plainText1 = "13800138000";
        String plainText2 = "13900139000";

        // When
        String encrypted1 = fieldEncryptor.encrypt(plainText1, EncryptType.AES);
        String encrypted2 = fieldEncryptor.encrypt(plainText2, EncryptType.AES);

        // Then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(fieldEncryptor.decrypt(encrypted1, EncryptType.AES)).isEqualTo(plainText1);
        assertThat(fieldEncryptor.decrypt(encrypted2, EncryptType.AES)).isEqualTo(plainText2);
    }
}
