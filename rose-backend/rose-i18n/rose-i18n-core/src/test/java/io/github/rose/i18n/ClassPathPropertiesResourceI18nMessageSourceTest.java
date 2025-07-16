package io.github.rose.i18n;

import io.github.rose.i18n.spi.ClassPathPropertiesResourceI18nMessageSource;
import io.github.rose.i18n.util.I18nUtils;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathPropertiesResourceI18nMessageSourceTest {

    private ClassPathPropertiesResourceI18nMessageSource messageSource;

    @BeforeAll
    static void beforeClass() {
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
    }

    @BeforeEach
    void setUp() {
        I18nUtils.destroyI18nMessageSource();
        messageSource = new ClassPathPropertiesResourceI18nMessageSource("test");
        messageSource.init();
    }

    @AfterEach
    void tearDown() {
        if (messageSource != null) {
            try {
                messageSource.destroy();
            } catch (Exception e) {
                // ignore
            }
        }
        I18nUtils.destroyI18nMessageSource();
    }

    @Test
    void testBasicMessageRetrieval() {
        // 中文
        assertEquals("测试消息", messageSource.getMessage("test.message", Locale.SIMPLIFIED_CHINESE));
        assertEquals("欢迎使用 Rose 国际化框架", messageSource.getMessage("test.welcome", Locale.SIMPLIFIED_CHINESE));
        // 英文
        assertEquals("Test Message", messageSource.getMessage("test.message", Locale.ENGLISH));
        assertEquals("Welcome to Rose I18n Framework", messageSource.getMessage("test.welcome", Locale.ENGLISH));
        // 查不到
        assertNull(messageSource.getMessage("not.exist.code", Locale.ENGLISH));
        // 空值
        assertEquals("", messageSource.getMessage("test.empty.value", Locale.ENGLISH));
    }

    @Test
    void testParameterizedMessages() {
        // 单参数
        assertEquals("Hello, John!", messageSource.getMessage("test.greeting", Locale.ENGLISH, "John"));
        assertEquals("你好，张三！", messageSource.getMessage("test.greeting", Locale.SIMPLIFIED_CHINESE, "张三"));
        // 多参数
        assertEquals("Name: John, Age: 25", messageSource.getMessage("test.parameter.multiple", Locale.ENGLISH, "John", "25"));
        // 参数数量不匹配
        assertEquals("Hello, {0}!", messageSource.getMessage("test.greeting", Locale.ENGLISH));
        // 特殊参数
        assertEquals("Hello,Rose", messageSource.getMessage("test.hello", Locale.ENGLISH, "Rose"));
    }

    @Test
    void testNullAndUnsupportedLocale() {
        // null code
        assertNull(messageSource.getMessage(null, Locale.ENGLISH));
        // null locale（应 fallback 到默认）
        assertEquals("测试消息", messageSource.getMessage("test.message", null));
        // unsupported locale fallback
        assertEquals("测试消息", messageSource.getMessage("test.message", Locale.FRENCH));
    }
}