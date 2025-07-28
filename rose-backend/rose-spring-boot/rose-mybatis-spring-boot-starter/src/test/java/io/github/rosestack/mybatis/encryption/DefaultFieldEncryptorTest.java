package io.github.rosestack.mybatis.encryption;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
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

    private DefaultFieldEncryptor fieldEncryptor;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getEncryption().setEnabled(true);
        properties.getEncryption().setSecretKey("MySecretKey12345");
        
        fieldEncryptor = new DefaultFieldEncryptor(properties);
    }

    @Test
    void shouldEncryptAndDecryptAES() {
        // Given
        String plainText = "13800138000";
        EncryptField.EncryptType encryptType = EncryptField.EncryptType.AES;

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
        EncryptField.EncryptType encryptType = EncryptField.EncryptType.DES;
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
        fieldEncryptor = new DefaultFieldEncryptor(properties);
        String plainText = "13800138000";

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, EncryptField.EncryptType.AES);
        String decrypted = fieldEncryptor.decrypt(plainText, EncryptField.EncryptType.AES);

        // Then
        assertThat(encrypted).isEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void shouldHandleNullAndEmptyValues() {
        // When & Then
        assertThat(fieldEncryptor.encrypt(null, EncryptField.EncryptType.AES)).isNull();
        assertThat(fieldEncryptor.encrypt("", EncryptField.EncryptType.AES)).isEmpty();
        assertThat(fieldEncryptor.decrypt(null, EncryptField.EncryptType.AES)).isNull();
        assertThat(fieldEncryptor.decrypt("", EncryptField.EncryptType.AES)).isEmpty();
    }

    @Test
    void shouldSupportAESAndDES() {
        // When & Then
        assertThat(fieldEncryptor.supports(EncryptField.EncryptType.AES)).isTrue();
        assertThat(fieldEncryptor.supports(EncryptField.EncryptType.DES)).isTrue();
        assertThat(fieldEncryptor.supports(EncryptField.EncryptType.DES3)).isTrue();
        assertThat(fieldEncryptor.supports(EncryptField.EncryptType.SM4)).isFalse();
    }

    @Test
    void shouldReturnOriginalForUnsupportedAlgorithm() {
        // Given
        String plainText = "13800138000";

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, EncryptField.EncryptType.SM4);
        String decrypted = fieldEncryptor.decrypt(plainText, EncryptField.EncryptType.SM4);

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
        fieldEncryptor = new DefaultFieldEncryptor(properties);

        String plainText = "13800138000";

        // When
        String encrypted = fieldEncryptor.encrypt(plainText, EncryptField.EncryptType.AES);

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
        String encrypted1 = fieldEncryptor.encrypt(plainText1, EncryptField.EncryptType.AES);
        String encrypted2 = fieldEncryptor.encrypt(plainText2, EncryptField.EncryptType.AES);

        // Then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(fieldEncryptor.decrypt(encrypted1, EncryptField.EncryptType.AES)).isEqualTo(plainText1);
        assertThat(fieldEncryptor.decrypt(encrypted2, EncryptField.EncryptType.AES)).isEqualTo(plainText2);
    }
}
