package io.github.rosestack.core.jackson.desensitization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 脱敏功能单元测试
 * <p>
 * 直接测试 Desensitization 类的静态方法，不依赖其他组件
 * </p>
 */
class DesensitizationTest {

    @Test
    void testLicensePlateDesensitization() {
        // 测试普通车牌
        String result1 = Desensitization.desensitizeLicensePlate("京A12345");
        assertEquals("京A***45", result1);

        // 测试新能源车牌
        String result2 = Desensitization.desensitizeLicensePlate("京AD12345");
        assertEquals("京A***45", result2);

        // 测试无效车牌
        String invalid = Desensitization.desensitizeLicensePlate("INVALID");
        assertEquals("INVALID", invalid);

        // 测试空值
        assertNull(Desensitization.desensitizeLicensePlate(null));
        assertEquals("", Desensitization.desensitizeLicensePlate(""));
    }

    @Test
    void testIpv4Desensitization() {
        // 测试标准IPv4地址
        String result1 = Desensitization.desensitizeIpv4("192.168.1.100");
        assertEquals("192.168.***.***", result1);

        // 测试公网IP
        String result2 = Desensitization.desensitizeIpv4("8.8.8.8");
        assertEquals("8.8.***.***", result2);

        // 测试无效IP
        String invalid = Desensitization.desensitizeIpv4("999.999.999.999");
        assertEquals("999.999.999.999", invalid);

        // 测试空值
        assertNull(Desensitization.desensitizeIpv4(null));
        assertEquals("", Desensitization.desensitizeIpv4(""));
    }

    @Test
    void testMaskLengthOptimization() {
        // 测试长银行卡号（19位）
        String longBankCard = "1234567890123456789";
        String result = Desensitization.desensitizeBankCard(longBankCard);

        // 验证脱敏符号不超过3个
        String maskPart = result.substring(4, result.length() - 4);
        assertTrue(maskPart.length() <= 3, "脱敏符号长度应该不超过3个");
        assertEquals("1234***6789", result);
    }

    @Test
    void testCustomMaskCharacter() {
        // 测试车牌号自定义脱敏字符
        String plate = Desensitization.desensitizeLicensePlate("京A12345", '#');
        assertEquals("京A###45", plate);

        // 测试IPv4自定义脱敏字符
        String ip = Desensitization.desensitizeIpv4("192.168.1.100", 'X');
        assertEquals("192.168.XXX.XXX", ip);
    }

    @Test
    void testOriginalFunctions() {
        // 测试原有功能是否正常
        assertEquals("张*丰", Desensitization.desensitizeName("张三丰"));
        assertEquals("张*", Desensitization.desensitizeName("张三"));
        assertEquals("138***8000", Desensitization.desensitizePhone("13800138000"));
        assertEquals("tes***@example.com", Desensitization.desensitizeEmail("test@example.com"));
    }
}
