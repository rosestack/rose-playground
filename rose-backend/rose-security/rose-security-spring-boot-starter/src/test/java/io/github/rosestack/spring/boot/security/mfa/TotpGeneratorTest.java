package io.github.rosestack.spring.boot.security.mfa;

import static org.junit.jupiter.api.Assertions.*;

import io.github.rosestack.spring.boot.security.mfa.totp.TotpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * TOTP生成器测试
 *
 * @author chensoul
 * @since 1.0.0
 */
public class TotpGeneratorTest {

    private TotpGenerator totpGenerator;

    @BeforeEach
    void setUp() {
        totpGenerator = new TotpGenerator();
    }

    @Test
    void testGenerateSecret() {
        String secret = totpGenerator.generateSecret();

        assertNotNull(secret);
        assertFalse(secret.isEmpty());
        // Base32编码的密钥应该只包含大写字母和数字2-7
        assertTrue(secret.matches("[A-Z2-7]+"));
    }

    @Test
    void testGenerateCode() {
        String secret = "JBSWY3DPEHPK3PXP"; // 测试用密钥
        String code = totpGenerator.generateCode(secret);

        assertNotNull(code);
        assertEquals(6, code.length());
        // 验证码应该只包含数字
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void testVerifyCode() {
        String secret = "JBSWY3DPEHPK3PXP";
        long timestamp = System.currentTimeMillis() / 1000;

        // 生成当前时间戳的验证码
        String code = totpGenerator.generateCode(secret, timestamp);

        // 验证应该成功
        assertTrue(totpGenerator.verifyCode(secret, code, timestamp));

        // 错误的验证码应该失败
        assertFalse(totpGenerator.verifyCode(secret, "000000", timestamp));

        // 空验证码应该失败
        assertFalse(totpGenerator.verifyCode(secret, "", timestamp));
        assertFalse(totpGenerator.verifyCode(secret, null, timestamp));
    }

    @Test
    void testVerifyCodeWithTimeWindow() {
        String secret = "JBSWY3DPEHPK3PXP";
        long timestamp = System.currentTimeMillis() / 1000;

        // 生成当前时间戳的验证码
        String code = totpGenerator.generateCode(secret, timestamp);

        // 在时间窗口内的验证应该成功（前后30秒）
        assertTrue(totpGenerator.verifyCode(secret, code, timestamp - 30));
        assertTrue(totpGenerator.verifyCode(secret, code, timestamp + 30));

        // 超出时间窗口的验证应该失败
        assertFalse(totpGenerator.verifyCode(secret, code, timestamp - 90));
        assertFalse(totpGenerator.verifyCode(secret, code, timestamp + 90));
    }

    @Test
    void testGenerateQrCodeData() {
        String secret = "JBSWY3DPEHPK3PXP";
        String accountName = "test@example.com";
        String issuer = "Rose Security";

        String qrCodeData = totpGenerator.generateQrCodeData(secret, accountName, issuer);

        assertNotNull(qrCodeData);
        assertTrue(qrCodeData.startsWith("otpauth://totp/"));
        assertTrue(qrCodeData.contains("secret=" + secret));
        assertTrue(qrCodeData.contains("issuer=" + issuer));
        assertTrue(qrCodeData.contains("digits=6"));
        assertTrue(qrCodeData.contains("period=30"));
    }

    @Test
    void testGetRemainingTime() {
        int remainingTime = totpGenerator.getRemainingTime();

        // 剩余时间应该在0-30秒之间
        assertTrue(remainingTime >= 0);
        assertTrue(remainingTime <= 30);
    }

    @Test
    void testGetters() {
        assertEquals(30, totpGenerator.getTimeStep());
        assertEquals(6, totpGenerator.getDigits());
        assertEquals(1, totpGenerator.getWindowSize());
    }

    @Test
    void testConsecutiveCodesDifferent() {
        String secret = "JBSWY3DPEHPK3PXP";
        long timestamp1 = 1000000; // 第一个时间点
        long timestamp2 = 1000030; // 30秒后（下一个时间窗口）

        String code1 = totpGenerator.generateCode(secret, timestamp1);
        String code2 = totpGenerator.generateCode(secret, timestamp2);

        // 不同时间窗口的验证码应该不同
        assertNotEquals(code1, code2);
    }

    @Test
    void testSameTimestampSameCode() {
        String secret = "JBSWY3DPEHPK3PXP";
        long timestamp = System.currentTimeMillis() / 1000;

        String code1 = totpGenerator.generateCode(secret, timestamp);
        String code2 = totpGenerator.generateCode(secret, timestamp);

        // 相同时间戳应该生成相同的验证码
        assertEquals(code1, code2);
    }
}
