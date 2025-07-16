package io.github.rose.i18n;

import io.github.rose.i18n.spi.ClassPathPropertiesResourceI18nMessageSource;
import io.github.rose.i18n.util.I18nUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathPropertiesResourceI18nMessageSourceTest {

    private ClassPathPropertiesResourceI18nMessageSource messageSource;

    @BeforeEach
    void setUp() {
        I18nMessageSourceManager.destroy();
        messageSource = new ClassPathPropertiesResourceI18nMessageSource("test");
        messageSource.init();
    }

    @AfterEach
    void tearDown() {
        if (messageSource != null) {
            messageSource.destroy();
            messageSource = null;
        }
        I18nMessageSourceManager.destroy();
    }

    @Test
    void testBasicMessageRetrieval() {
        assertEquals("测试消息", messageSource.getMessage("test.message", Locale.SIMPLIFIED_CHINESE));
        assertEquals("欢迎使用 Rose 国际化框架", messageSource.getMessage("test.welcome", Locale.SIMPLIFIED_CHINESE));
        assertEquals("Test Message", messageSource.getMessage("test.message", Locale.ENGLISH));
        assertEquals("Welcome to Rose I18n Framework", messageSource.getMessage("test.welcome", Locale.ENGLISH));
        assertNull(messageSource.getMessage("not.exist.code", Locale.ENGLISH));
        assertEquals("", messageSource.getMessage("test.empty.value", Locale.ENGLISH));
    }

    @Test
    void testParameterizedMessages() {
        assertEquals("Hello, John!", messageSource.getMessage("test.greeting", Locale.ENGLISH, "John"));
        assertEquals("你好，张三！", messageSource.getMessage("test.greeting", Locale.SIMPLIFIED_CHINESE, "张三"));
        assertEquals("Name: John, Age: 25", messageSource.getMessage("test.parameter.multiple", Locale.ENGLISH, "John", "25"));
        assertEquals("Hello, {0}!", messageSource.getMessage("test.greeting", Locale.ENGLISH));
        assertEquals("Hello,Rose", messageSource.getMessage("test.hello", Locale.ENGLISH, "Rose"));
    }

    @Test
    void testNullAndUnsupportedLocale() {
        assertNull(messageSource.getMessage(null, Locale.ENGLISH));
        assertEquals("测试消息", messageSource.getMessage("test.message", null));
        assertEquals("测试消息", messageSource.getMessage("test.message", Locale.FRENCH));
    }
}