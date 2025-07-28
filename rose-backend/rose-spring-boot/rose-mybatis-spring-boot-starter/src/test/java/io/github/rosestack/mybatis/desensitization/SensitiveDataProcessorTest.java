package io.github.rosestack.mybatis.desensitization;

import io.github.rosestack.mybatis.annotation.SensitiveField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 敏感数据脱敏处理器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class SensitiveDataProcessorTest {

    @Test
    void shouldDesensitizePhone() {
        // Given
        String phone = "13800138000";

        // When
        String result = SensitiveDataProcessor.desensitize(phone, SensitiveField.SensitiveType.PHONE, null);

        // Then
        assertThat(result).isEqualTo("138****8000");
    }

    @Test
    void shouldDesensitizeIdCard() {
        // Given
        String idCard = "110101199001011234";

        // When
        String result = SensitiveDataProcessor.desensitize(idCard, SensitiveField.SensitiveType.ID_CARD, null);

        // Then
        assertThat(result).isEqualTo("110101****1234");
    }

    @Test
    void shouldDesensitizeEmail() {
        // Given
        String email = "test@example.com";

        // When
        String result = SensitiveDataProcessor.desensitize(email, SensitiveField.SensitiveType.EMAIL, null);

        // Then
        assertThat(result).isEqualTo("tes***@example.com");
    }

    @Test
    void shouldDesensitizeBankCard() {
        // Given
        String bankCard = "6222021234567890";

        // When
        String result = SensitiveDataProcessor.desensitize(bankCard, SensitiveField.SensitiveType.BANK_CARD, null);

        // Then
        assertThat(result).isEqualTo("6222****7890");
    }

    @Test
    void shouldDesensitizeName() {
        // Given
        String name = "张三丰";

        // When
        String result = SensitiveDataProcessor.desensitize(name, SensitiveField.SensitiveType.NAME, null);

        // Then
        assertThat(result).isEqualTo("张*丰");
    }

    @Test
    void shouldDesensitizeAddress() {
        // Given
        String address = "北京市朝阳区建国门外大街1号";

        // When
        String result = SensitiveDataProcessor.desensitize(address, SensitiveField.SensitiveType.ADDRESS, null);

        // Then
        assertThat(result).isEqualTo("北京市***号");
    }

    @Test
    void shouldDesensitizeCustom() {
        // Given
        String value = "1234567890";
        String rule = "2,3";

        // When
        String result = SensitiveDataProcessor.desensitize(value, SensitiveField.SensitiveType.CUSTOM, rule);

        // Then
        assertThat(result).isEqualTo("12*****890");
    }

    @Test
    void shouldHandleNullAndEmptyValues() {
        // When & Then
        assertThat(SensitiveDataProcessor.desensitize(null, SensitiveField.SensitiveType.PHONE, null)).isNull();
        assertThat(SensitiveDataProcessor.desensitize("", SensitiveField.SensitiveType.PHONE, null)).isEmpty();
        assertThat(SensitiveDataProcessor.desensitize("   ", SensitiveField.SensitiveType.PHONE, null)).isEqualTo("   ");
    }

    @Test
    void shouldHandleShortValues() {
        // Given
        String shortPhone = "123";

        // When
        String result = SensitiveDataProcessor.desensitize(shortPhone, SensitiveField.SensitiveType.PHONE, null);

        // Then
        assertThat(result).isEqualTo("123"); // 太短的值不脱敏
    }
}
