package io.github.rose.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    // ========== BusinessException 基本构造器测试 ==========

    @Test
    void testSimpleMessageConstructor() {
        BusinessException exception = new BusinessException("Simple error message");

        assertNull(exception.getMessageCode());
        assertNull(exception.getMessageArgs());
        assertEquals("Simple error message", exception.getDefaultMessage());
        assertFalse(exception.isNeedsInternationalization());
        assertEquals("Simple error message", exception.getMessage());
    }

    @Test
    void testSimpleMessageWithCauseConstructor() {
        Throwable cause = new IllegalArgumentException("Root cause");
        BusinessException exception = new BusinessException("Error with cause", cause);

        assertNull(exception.getMessageCode());
        assertEquals("Error with cause", exception.getDefaultMessage());
        assertFalse(exception.isNeedsInternationalization());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testI18nConstructor() {
        BusinessException exception = new BusinessException("user.not.found", new Object[]{"user123"});

        assertEquals("user.not.found", exception.getMessageCode());
        assertArrayEquals(new String[]{"user123"}, exception.getMessageArgs());
        assertTrue(exception.isNeedsInternationalization());
        assertEquals("user.not.found", exception.getMessage()); // 没有默认消息时返回messageCode
    }

    @Test
    void testI18nWithDefaultMessageConstructor() {
        BusinessException exception = new BusinessException("user.not.found", "User not found", new Object[]{"user123"});

        assertEquals("user.not.found", exception.getMessageCode());
        assertEquals("User not found", exception.getDefaultMessage());
        assertArrayEquals(new String[]{"user123"}, exception.getMessageArgs());
        assertTrue(exception.isNeedsInternationalization());
        assertEquals("User not found", exception.getMessage()); // 有默认消息时返回defaultMessage
    }

    // ========== 静态工厂方法测试 ==========

    @Test
    void testStaticFactoryOf() {
        BusinessException exception = BusinessException.of("Factory error");

        assertNull(exception.getMessageCode());
        assertEquals("Factory error", exception.getDefaultMessage());
        assertFalse(exception.isNeedsInternationalization());
    }

    @Test
    void testStaticFactoryI18n() {
        BusinessException exception = BusinessException.i18n("validation.failed", "email", new Object[]{"email", "required"});

        assertEquals("validation.failed", exception.getMessageCode());
        assertArrayEquals(new Object[]{"email", "required"}, exception.getMessageArgs());
        assertTrue(exception.isNeedsInternationalization());
    }

    // ========== RateLimitException 继承测试 ==========

    @Test
    void testRateLimitExceptionSimple() {
        RateLimitException exception = new RateLimitException("Rate limit exceeded");

        assertNull(exception.getMessageCode());
        assertEquals("Rate limit exceeded", exception.getDefaultMessage());
        assertFalse(exception.isNeedsInternationalization());
        assertTrue(exception instanceof BusinessException);
    }

    @Test
    void testRateLimitExceptionI18n() {
        RateLimitException exception = new RateLimitException("rate.limit.exceeded", "user123", new Object[]{"user123", "100"});

        assertEquals("rate.limit.exceeded", exception.getMessageCode());
        assertArrayEquals(new Object[]{"user123", "100"}, exception.getMessageArgs());
        assertTrue(exception.isNeedsInternationalization());
        assertTrue(exception instanceof BusinessException);
    }

    @Test
    void testRateLimitExceptionI18nWithDefault() {
        RateLimitException exception = new RateLimitException(
                "rate.limit.exceeded",
                "Rate limit exceeded",
                new String[]{"user123", "100"}
        );

        assertEquals("rate.limit.exceeded", exception.getMessageCode());
        assertEquals("Rate limit exceeded", exception.getDefaultMessage());
        assertArrayEquals(new Object[]{"user123", "100"}, exception.getMessageArgs());
        assertTrue(exception.isNeedsInternationalization());
    }

    // ========== 使用场景验证 ==========

    @Test
    void testUsageScenarios() {
        // 场景1: 简单错误，不需要国际化
        BusinessException simpleError = new BusinessException("配置文件不存在");
        assertFalse(simpleError.isNeedsInternationalization());

        // 场景2: 国际化错误，面向用户
        BusinessException userError = new BusinessException("user.not.found", "用户不存在", new Object[]{"user123"});
        assertTrue(userError.isNeedsInternationalization());

        // 场景3: 限流异常，简单消息
        RateLimitException rateLimitSimple = new RateLimitException("请求过于频繁");
        assertFalse(rateLimitSimple.isNeedsInternationalization());

        // 场景4: 限流异常，国际化
        RateLimitException rateLimitI18n = new RateLimitException("rate.limit.exceeded", "请求过于频繁", new Object[]{"user123"});
        assertTrue(rateLimitI18n.isNeedsInternationalization());
    }

    // ========== API 清晰性验证 ==========

    @Test
    void testApiClarity() {
        // 构造器语义清晰
        BusinessException simple = new BusinessException("简单错误");
        BusinessException i18nOnly = new BusinessException("error.code", new Object[]{"arg1"});
        BusinessException i18nWithDefault = new BusinessException("error.code", "默认消息", new Object[]{"arg1"});

        // 静态工厂方法语义清晰
        BusinessException factorySimple = BusinessException.of("工厂错误");
        BusinessException factoryI18n = BusinessException.i18n("error.code", new Object[]{"arg1"});

        // 继承类使用简单
        RateLimitException rateLimitSimple = new RateLimitException("限流错误");
        RateLimitException rateLimitI18n = new RateLimitException("rate.limit", new Object[]{"arg1"});

        // 验证所有实例都是有效的
        assertNotNull(simple.getMessage());
        assertNotNull(i18nOnly.getMessage());
        assertNotNull(i18nWithDefault.getMessage());
        assertNotNull(factorySimple.getMessage());
        assertNotNull(factoryI18n.getMessage());
        assertNotNull(rateLimitSimple.getMessage());
        assertNotNull(rateLimitI18n.getMessage());
    }

    // ========== 向后兼容性测试 ==========

    @Test
    void testBackwardCompatibility() {
        // 原有的简单构造器仍然可用
        BusinessException oldStyle = new BusinessException("旧式错误消息");
        assertEquals("旧式错误消息", oldStyle.getMessage());

        // 原有的继承方式仍然可用
        RateLimitException oldRateLimit = new RateLimitException("旧式限流错误");
        assertEquals("旧式限流错误", oldRateLimit.getMessage());
        assertTrue(oldRateLimit instanceof BusinessException);
    }

    // ========== 边界情况测试 ==========

    @Test
    void testEdgeCases() {
        // 空消息
        BusinessException emptyMessage = new BusinessException("");
        assertEquals("", emptyMessage.getMessage());

        // null 消息
        BusinessException nullMessage = new BusinessException(null);
        assertNull(nullMessage.getMessage());

        // 空参数数组
        BusinessException emptyArgs = new BusinessException("test.code");
        assertEquals(0, emptyArgs.getMessageArgsCount());

        // 单个参数
        BusinessException singleArg = new BusinessException("test.code", new Object[]{"arg1"});
        assertEquals(1, singleArg.getMessageArgsCount());
    }
}
