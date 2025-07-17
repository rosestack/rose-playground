package io.github.rose.i18n.spring;

import io.github.rose.i18n.I18nCompositeMessageSource;
import io.github.rose.i18n.builder.I18nMessageSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring MessageSource适配器测试类
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
class SpringI18nMessageSourceAdapterTest {

    private MessageSource messageSource;
    private I18nCompositeMessageSource i18nMessageSource;

    @BeforeEach
    void setUp() {
        i18nMessageSource = I18nMessageSourceBuilder.create()
                .addPropertiesProvider("i18n_messages", "classpath:META-INF/i18n/properties/")
                .addJsonProvider("i18n_messages", "classpath:META-INF/i18n/json/")
                .addYamlProvider("i18n_messages", "classpath:META-INF/i18n/yaml/")
                .setDefaultLocale(Locale.ENGLISH)
                .setSupportedLocales(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)
                .enableCache(true)
                .build();

        messageSource = new SpringI18nMessageSourceAdapter(i18nMessageSource);
    }

    @Test
    void testGetMessageWithDefault() {
        // 测试带默认值的消息获取
        String message = messageSource.getMessage("test.message", null, "Default", Locale.ENGLISH);
        assertEquals("Hello", message);

        // 测试不存在的消息键，返回默认值
        message = messageSource.getMessage("non.existent.key", null, "Default Message", Locale.ENGLISH);
        assertEquals("Default Message", message);
    }

    @Test
    void testGetMessageWithArgs() {
        // 测试带参数的消息获取
        String message = messageSource.getMessage("test.param", new Object[]{"World"}, Locale.ENGLISH);
        assertEquals("Hello, World", message);

        message = messageSource.getMessage("test.param", new Object[]{"世界"}, Locale.SIMPLIFIED_CHINESE);
        assertEquals("你好, 世界", message);
    }

    @Test
    void testGetMessageNotFound() {
        // 测试不存在的消息键，抛出Spring异常
        assertThrows(NoSuchMessageException.class, () -> {
            messageSource.getMessage("non.existent.key", null, Locale.ENGLISH);
        });
    }

    @Test
    void testGetMessageWithResolvable() {
        // 测试使用MessageSourceResolvable
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                new String[]{"test.message"}, 
                null, 
                "Default Message"
        );

        String message = messageSource.getMessage(resolvable, Locale.ENGLISH);
        assertEquals("Hello", message);
    }

    @Test
    void testGetMessageWithResolvableMultipleCodes() {
        // 测试使用MessageSourceResolvable，多个代码
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                new String[]{"non.existent.key1", "non.existent.key2", "test.message"}, 
                null, 
                "Default Message"
        );

        String message = messageSource.getMessage(resolvable, Locale.ENGLISH);
        assertEquals("Hello", message); // 应该找到第三个代码
    }

    @Test
    void testGetMessageWithResolvableDefaultMessage() {
        // 测试使用MessageSourceResolvable，所有代码都不存在，返回默认消息
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                new String[]{"non.existent.key1", "non.existent.key2"}, 
                null, 
                "Default Message"
        );

        String message = messageSource.getMessage(resolvable, Locale.ENGLISH);
        assertEquals("Default Message", message);
    }

    @Test
    void testGetMessageWithResolvableNoDefault() {
        // 测试使用MessageSourceResolvable，所有代码都不存在，没有默认消息
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                new String[]{"non.existent.key1", "non.existent.key2"}, 
                null, 
                null
        );

        assertThrows(NoSuchMessageException.class, () -> {
            messageSource.getMessage(resolvable, Locale.ENGLISH);
        });
    }

    @Test
    void testGetMessageWithResolvableArgs() {
        // 测试使用MessageSourceResolvable，带参数
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                new String[]{"test.param"}, 
                new Object[]{"World"}, 
                "Default Message"
        );

        String message = messageSource.getMessage(resolvable, Locale.ENGLISH);
        assertEquals("Hello, World", message);
    }

    @Test
    void testNullLocale() {
        // 测试null语言环境，应该使用默认语言环境
        String message = messageSource.getMessage("test.message", null, "Default", null);
        assertEquals("Hello", message); // 应该使用默认语言环境（英文）
    }

    @Test
    void testNullCode() {
        // 测试null消息代码
        String message = messageSource.getMessage(null, null, "Default", Locale.ENGLISH);
        assertEquals("Default", message);

        assertThrows(NoSuchMessageException.class, () -> {
            messageSource.getMessage(null, null, Locale.ENGLISH);
        });
    }

    @Test
    void testAdapterMethods() {
        SpringI18nMessageSourceAdapter adapter = (SpringI18nMessageSourceAdapter) messageSource;

        // 测试适配器特有的方法
        assertTrue(adapter.containsMessage("test.message", Locale.ENGLISH));
        assertFalse(adapter.containsMessage("non.existent.key", Locale.ENGLISH));

        assertEquals(Locale.ENGLISH, adapter.getDefaultLocale());
        assertTrue(adapter.getSupportedLocales().contains(Locale.ENGLISH));

        assertNotNull(adapter.getAllMessages(Locale.ENGLISH));
        assertFalse(adapter.getAllMessages(Locale.ENGLISH).isEmpty());

        assertDoesNotThrow(() -> adapter.refresh());

        assertSame(i18nMessageSource, adapter.getI18nMessageSource());
    }
}
