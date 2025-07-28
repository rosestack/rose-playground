package io.github.rosestack.mybatis.desensitization;

import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.enums.SensitiveType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单脱敏测试
 * <p>
 * 验证基础的脱敏功能正常工作
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
class SimpleDesensitizationTest {

    @Test
    void testBasicDesensitization() {
        log.info("=== 测试基础脱敏功能 ===");
        
        // 测试姓名脱敏
        String nameResult = SensitiveDataProcessor.desensitizeName("张三丰");
        assertEquals("张*丰", nameResult);
        
        // 测试手机号脱敏
        String phoneResult = SensitiveDataProcessor.desensitizePhone("13800138000");
        assertEquals("138****8000", phoneResult);
        
        // 测试邮箱脱敏
        String emailResult = SensitiveDataProcessor.desensitizeEmail("test@example.com");
        assertEquals("tes***@example.com", emailResult);
        
        // 测试身份证脱敏
        String idCardResult = SensitiveDataProcessor.desensitizeIdCard("110101199001011234");
        assertEquals("110101****1234", idCardResult);
        
        // 测试银行卡脱敏
        String bankCardResult = SensitiveDataProcessor.desensitizeBankCard("6222021234567890");
        assertEquals("6222****7890", bankCardResult);
        
        // 测试地址脱敏
        String addressResult = SensitiveDataProcessor.desensitizeAddress("北京市朝阳区建国门外大街1号");
        assertEquals("北京市***号", addressResult);
        
        log.info("基础脱敏测试通过");
        log.info("姓名: {} -> {}", "张三丰", nameResult);
        log.info("手机: {} -> {}", "13800138000", phoneResult);
        log.info("邮箱: {} -> {}", "test@example.com", emailResult);
        log.info("身份证: {} -> {}", "110101199001011234", idCardResult);
        log.info("银行卡: {} -> {}", "6222021234567890", bankCardResult);
        log.info("地址: {} -> {}", "北京市朝阳区建国门外大街1号", addressResult);
    }

    @Test
    void testCustomDesensitization() {
        log.info("=== 测试自定义脱敏功能 ===");
        
        // 测试自定义脱敏
        String customResult1 = SensitiveDataProcessor.desensitizeCustom("1234567890", "2,3");
        assertEquals("12*****890", customResult1);
        
        String customResult2 = SensitiveDataProcessor.desensitizeCustom("abcdefgh", "1,1");
        assertEquals("a******h", customResult2);
        
        String customResult3 = SensitiveDataProcessor.desensitizeCustom("test", "1,1");
        assertEquals("t**t", customResult3);
        
        log.info("自定义脱敏测试通过");
        log.info("自定义1: {} -> {}", "1234567890", customResult1);
        log.info("自定义2: {} -> {}", "abcdefgh", customResult2);
        log.info("自定义3: {} -> {}", "test", customResult3);
    }

    @Test
    void testEdgeCases() {
        log.info("=== 测试边界情况 ===");
        
        // 测试空值和null
        assertNull(SensitiveDataProcessor.desensitizeName(null));
        assertEquals("", SensitiveDataProcessor.desensitizeName(""));
        assertEquals(" ", SensitiveDataProcessor.desensitizeName(" "));
        
        // 测试单字符
        assertEquals("张", SensitiveDataProcessor.desensitizeName("张"));
        
        // 测试短字符串
        assertEquals("张*", SensitiveDataProcessor.desensitizeName("张三"));
        
        // 测试无效手机号
        String invalidPhone = SensitiveDataProcessor.desensitizePhone("123");
        assertEquals("123", invalidPhone); // 无效格式应该返回原值
        
        // 测试无效邮箱
        String invalidEmail = SensitiveDataProcessor.desensitizeEmail("invalid");
        assertEquals("invalid", invalidEmail); // 无效格式应该返回原值
        
        log.info("边界情况测试通过");
    }

    @Test
    void testObjectDesensitization() {
        log.info("=== 测试对象脱敏功能 ===");
        
        // 创建测试对象
        TestUser user = new TestUser();
        user.setName("李四");
        user.setPhone("13900139000");
        user.setEmail("lisi@test.com");
        
        // 执行对象脱敏
        Object result = SensitiveDataProcessor.desensitizeObject(user);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof TestUser);
        
        TestUser desensitizedUser = (TestUser) result;
        assertEquals("李*", desensitizedUser.getName());
        assertEquals("139****9000", desensitizedUser.getPhone());
        assertEquals("lis***@test.com", desensitizedUser.getEmail());
        
        log.info("对象脱敏测试通过");
        log.info("原始用户: name={}, phone={}, email={}", user.getName(), user.getPhone(), user.getEmail());
        log.info("脱敏用户: name={}, phone={}, email={}", desensitizedUser.getName(), desensitizedUser.getPhone(), desensitizedUser.getEmail());
    }

    /**
     * 测试用户类
     */
    public static class TestUser {
        @SensitiveField(SensitiveType.NAME)
        private String name;

        @SensitiveField(SensitiveType.PHONE)
        private String phone;

        @SensitiveField(SensitiveType.EMAIL)
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}