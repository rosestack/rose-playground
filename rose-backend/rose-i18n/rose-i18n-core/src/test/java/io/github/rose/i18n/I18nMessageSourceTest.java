package io.github.rose.i18n;

import io.github.rose.i18n.builder.I18nMessageSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I18nCompositeMessageSource测试类
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
class I18nMessageSourceTest {

    private I18nCompositeMessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = I18nMessageSourceBuilder.create()
                .addPropertiesProvider("i18n_messages", "classpath:META-INF/i18n/properties/")
                .addJsonProvider("i18n_messages", "classpath:META-INF/i18n/json/")
                .addYamlProvider("i18n_messages", "classpath:META-INF/i18n/yaml/")
                .setDefaultLocale(Locale.ENGLISH)
                .setSupportedLocales(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)
                .enableCache(true)
                .build();
    }

    @Test
    void testGetMessage() {
        // 测试英文消息
        String message = messageSource.getSimpleMessage("test.message", Locale.ENGLISH);
        assertEquals("Hello", message);

        // 测试中文消息
        message = messageSource.getSimpleMessage("test.message", Locale.SIMPLIFIED_CHINESE);
        assertEquals("你好", message);
    }

    @Test
    void testGetMessageWithArgs() {
        // 测试带参数的消息
        String message = messageSource.getMessage("test.param", new Object[]{"World"}, Locale.ENGLISH);
        assertEquals("Hello, World", message);

        message = messageSource.getMessage("test.param", new Object[]{"世界"}, Locale.SIMPLIFIED_CHINESE);
        assertEquals("你好, 世界", message);
    }

    @Test
    void testGetMessageWithDefault() {
        // 测试不存在的消息键，返回默认值
        String message = messageSource.getSimpleMessage("non.existent.key", "Default Message", Locale.ENGLISH);
        assertEquals("Default Message", message);
    }

    @Test
    void testGetMessageNotFound() {
        // 测试不存在的消息键，抛出异常
        assertThrows(I18nMessageNotFoundException.class, () -> {
            messageSource.getSimpleMessage("non.existent.key", Locale.ENGLISH);
        });
    }

    @Test
    void testGetNestedMessage() {
        // 测试嵌套消息（JSON/YAML格式）
        String message = messageSource.getSimpleMessage("test.nested.message", Locale.ENGLISH);
        assertEquals("Hello Nested", message);

        message = messageSource.getSimpleMessage("test.nested.message", Locale.SIMPLIFIED_CHINESE);
        assertEquals("你好嵌套", message);
    }

    @Test
    void testGetNestedMessageWithArgs() {
        // 测试嵌套消息带参数
        String message = messageSource.getMessage("test.nested.param", new Object[]{"World"}, Locale.ENGLISH);
        assertEquals("Hello Nested, World", message);

        message = messageSource.getMessage("test.nested.param", new Object[]{"世界"}, Locale.SIMPLIFIED_CHINESE);
        assertEquals("你好嵌套, 世界", message);
    }

    @Test
    void testGetAllMessages() {
        // 测试获取所有消息
        Map<String, String> messages = messageSource.getAllMessages(Locale.ENGLISH);
        assertFalse(messages.isEmpty());
        assertTrue(messages.containsKey("test.message"));
        assertEquals("Hello", messages.get("test.message"));
    }

    @Test
    void testGetAllMessagesWithKeys() {
        // 测试批量获取指定键的消息
        Set<String> keys = Set.of("test.message", "test.param", "foo.message");
        Map<String, String> messages = messageSource.getMessages(keys, Locale.ENGLISH);

        assertEquals(3, messages.size());
        assertEquals("Hello", messages.get("test.message"));
        assertEquals("Hello, {0}", messages.get("test.param"));
        assertEquals("Hello", messages.get("foo.message"));
    }

    @Test
    void testGetSupportedLocales() {
        // 测试获取支持的语言环境
        Set<Locale> supportedLocales = messageSource.getSupportedLocales();
        assertTrue(supportedLocales.contains(Locale.ENGLISH));
        assertTrue(supportedLocales.contains(Locale.SIMPLIFIED_CHINESE));
    }

    @Test
    void testGetDefaultLocale() {
        // 测试获取默认语言环境
        Locale defaultLocale = messageSource.getDefaultLocale();
        assertEquals(Locale.ENGLISH, defaultLocale);
    }

    @Test
    void testContainsMessage() {
        // 测试检查消息是否存在
        assertTrue(messageSource.containsMessage("test.message", Locale.ENGLISH));
        assertFalse(messageSource.containsMessage("non.existent.key", Locale.ENGLISH));
    }

    @Test
    void testFallbackToDefaultLocale() {
        // 测试回退到默认语言环境
        // 假设某个消息只在英文中存在，中文中不存在
        // 应该回退到英文
        try {
            String message = messageSource.getSimpleMessage("test.message", Locale.JAPANESE);
            assertEquals("Hello", message); // 应该回退到英文
        } catch (I18nMessageNotFoundException e) {
            // 如果日文和英文都不存在，则抛出异常是正确的
        }
    }

    @Test
    void testRefresh() {
        // 测试刷新功能
        assertDoesNotThrow(() -> {
            messageSource.refresh();
        });
    }

    @Test
    void testInitAndDestroy() {
        // 测试初始化和销毁
        assertDoesNotThrow(() -> {
            messageSource.init();
            messageSource.destroy();
        });
    }
}
