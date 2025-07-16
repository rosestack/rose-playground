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
                // 忽略销毁过程中的异常
            }
        }
        I18nUtils.destroyI18nMessageSource();
    }

    @Test
    void testBasicMessageSourceProperties() {
        assertEquals("test", messageSource.getSource());
        assertEquals(Locale.SIMPLIFIED_CHINESE, messageSource.getDefaultLocale());
        assertTrue(messageSource.getPriority() >= 0);
        Set<Locale> supportedLocales = messageSource.getSupportedLocales();
        assertNotNull(supportedLocales);
        assertTrue(supportedLocales.contains(Locale.ENGLISH));
        assertTrue(supportedLocales.contains(Locale.SIMPLIFIED_CHINESE));
        String toString = messageSource.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test"));
        assertTrue(toString.contains("ClassPathPropertiesResourceI18nMessageSource"));
    }

    @Test
    void testChineseMessageRetrieval() {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        String message = messageSource.getMessage("test.message", locale);
        assertEquals("测试消息", message);
        String greeting = messageSource.getMessage("test.greeting", locale, "世界");
        assertEquals("你好，世界！", greeting);
        String welcome = messageSource.getMessage("test.welcome", locale);
        assertEquals("欢迎使用 Rose 国际化框架", welcome);
    }

    @Test
    void testEnglishMessageRetrieval() {
        Locale locale = Locale.ENGLISH;
        String message = messageSource.getMessage("test.message", locale);
        assertEquals("Test Message", message);
        String greeting = messageSource.getMessage("test.greeting", locale, "World");
        assertEquals("Hello, World!", greeting);
        String welcome = messageSource.getMessage("test.welcome", locale);
        assertEquals("Welcome to Rose I18n Framework", welcome);
    }

    @Test
    void testSupportedLocales() {
        Locale[] locales = {Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE};
        for (Locale locale : locales) {
            String message = messageSource.getMessage("test.message", locale);
            assertNotNull(message);
            assertFalse(message.isEmpty());
            String greeting = messageSource.getMessage("test.greeting", locale, "Test");
            assertNotNull(greeting);
            assertTrue(greeting.contains("Test") || greeting.contains("测试"));
        }
    }

    @Test
    void testMessageCodesAndValues() {
        Object[][] cases = {
                {"test.message", Locale.ENGLISH, "Test Message"},
                {"test.message", Locale.SIMPLIFIED_CHINESE, "测试消息"},
                {"test.common.save", Locale.ENGLISH, "Save"},
                {"test.common.save", Locale.SIMPLIFIED_CHINESE, "保存"},
        };
        for (Object[] c : cases) {
            String code = (String) c[0];
            Locale locale = (Locale) c[1];
            String expected = (String) c[2];
            String actual = messageSource.getMessage(code, locale);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testBatchMessageRetrieval() {
        Set<String> codes = Set.of("test.message", "test.common.save");
        Map<String, String> chineseMessages = messageSource.getMessages(codes, Locale.SIMPLIFIED_CHINESE);
        assertNotNull(chineseMessages);
        assertEquals(2, chineseMessages.size());
        assertEquals("测试消息", chineseMessages.get("test.message"));
        assertEquals("保存", chineseMessages.get("test.common.save"));
        Map<String, String> englishMessages = messageSource.getMessages(codes, Locale.ENGLISH);
        assertNotNull(englishMessages);
        assertEquals(2, englishMessages.size());
        assertEquals("Test Message", englishMessages.get("test.message"));
        assertEquals("Save", englishMessages.get("test.common.save"));
        Map<String, String> emptyResult = messageSource.getMessages(Collections.emptySet(), Locale.ENGLISH);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    void testGetAllMessages() {
        Map<String, String> allChineseMessages = messageSource.getMessages(Locale.SIMPLIFIED_CHINESE);
        assertNotNull(allChineseMessages);
        assertTrue(allChineseMessages.size() > 0);
        assertTrue(allChineseMessages.containsKey("test.message"));
        assertTrue(allChineseMessages.containsKey("test.common.save"));
        Map<String, String> allEnglishMessages = messageSource.getMessages(Locale.ENGLISH);
        assertNotNull(allEnglishMessages);
        assertTrue(allEnglishMessages.size() > 0);
        assertTrue(allEnglishMessages.containsKey("test.message"));
        assertTrue(allEnglishMessages.containsKey("test.common.save"));
        assertEquals(allChineseMessages.size(), allEnglishMessages.size());
    }

    @Test
    void testNonExistentMessageCode() {
        String message = messageSource.getMessage("not.exist.code", Locale.ENGLISH);
        assertNull(message);
    }

    @Test
    void testNullParameterHandling() {
        String message = messageSource.getMessage(null, Locale.ENGLISH);
        assertNull(message);
        String message2 = messageSource.getMessage("test.message", Locale.ENGLISH, null);
        assertEquals("Test Message", message2);
    }

    @Test
    void testEmptyStringAndSpecialCharacters() {
        String empty = messageSource.getMessage("test.empty.value", Locale.ENGLISH);
        assertEquals("", empty);
        String special = messageSource.getMessage("test.a", Locale.ENGLISH);
        assertEquals("test-a", special);
    }

    @Test
    void testUnsupportedLocale() {
        Locale unsupportedLocale = Locale.FRENCH;
        String message = messageSource.getMessage("test.message", unsupportedLocale);
        assertEquals("测试消息", message);
    }

    @Test
    void testSingleParameter() {
        String greeting = messageSource.getMessage("test.greeting", Locale.ENGLISH, "John");
        assertEquals("Hello, John!", greeting);
        String chineseGreeting = messageSource.getMessage("test.greeting", Locale.SIMPLIFIED_CHINESE, "张三");
        assertEquals("你好，张三！", chineseGreeting);
        String singleParam = messageSource.getMessage("test.parameter.single", Locale.ENGLISH, "Value");
        assertEquals("Parameter: Value", singleParam);
    }

    @Test
    void testMultipleParameters() {
        String validationError = messageSource.getMessage("test.error.validation", Locale.ENGLISH, "用户名不能为空");
        assertEquals("Validation Error: 用户名不能为空", validationError);
        String multipleParams = messageSource.getMessage("test.parameter.multiple", Locale.ENGLISH, "John", "25");
        assertEquals("Name: John, Age: 25", multipleParams);
    }

    @Test
    void testMismatchedParameterCount() {
        String message = messageSource.getMessage("test.greeting", Locale.ENGLISH);
        assertEquals("Hello, {0}!", message);
    }

    @Test
    void testSpecialParameterValues() {
        String message = messageSource.getMessage("test.hello", Locale.ENGLISH, "Rose");
        assertEquals("Hello,Rose", message);
    }

    @Test
    void testInitializationAndDestruction() {
        messageSource.destroy();
        messageSource.init();
        String message = messageSource.getMessage("test.message", Locale.ENGLISH);
        assertEquals("Test Message", message);
    }

    @Test
    void testBehaviorAfterDestruction() {
        messageSource.destroy();
        String message = messageSource.getMessage("test.message", Locale.ENGLISH);
        assertNull(message);
    }

    @Test
    void testPerformanceStability() {
        for (int i = 0; i < 3; i++) {
            String message = messageSource.getMessage("test.message", Locale.ENGLISH);
            assertEquals("Test Message", message);
        }
    }

    @Test
    void testComprehensiveFunctionality() {
        String message = messageSource.getMessage("test.message", Locale.ENGLISH);
        assertEquals("Test Message", message);
        String greeting = messageSource.getMessage("test.greeting", Locale.ENGLISH, "World");
        assertEquals("Hello, World!", greeting);
        String welcome = messageSource.getMessage("test.welcome", Locale.ENGLISH);
        assertEquals("Welcome to Rose I18n Framework", welcome);
    }
}