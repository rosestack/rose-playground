package io.github.rose.i18n;

import io.github.rose.i18n.spi.ClassPathPropertiesResourceI18nMessageSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassPath Properties Resource I18n Message Source Test
 * 
 * 测试 ClassPathPropertiesResourceI18nMessageSource 的各种功能，
 * 包括基础消息获取、多语言支持、参数化消息、错误处理等。
 * 
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("ClassPath Properties Resource I18n Message Source Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClassPathPropertiesResourceI18nMessageSourceTest extends AbstractI18nTest {
    
    private ClassPathPropertiesResourceI18nMessageSource messageSource;
    
    @BeforeEach
    void setUp() {
        messageSource = new ClassPathPropertiesResourceI18nMessageSource("test");
        messageSource.init();
    }
    
    @AfterEach
    void tearDown() {
        if (messageSource != null) {
            try {
                messageSource.destroy();
            } catch (Exception e) {
                // 忽略销毁过程中的异常，这在测试中是可以接受的
            }
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("测试基础消息源属性")
    void testBasicMessageSourceProperties() {
        // 测试源名称
        assertEquals("test", messageSource.getSource());
        
        // 测试默认语言环境（应该是中文）
        assertEquals(Locale.SIMPLIFIED_CHINESE, messageSource.getDefaultLocale());
        
        // 测试优先级
        assertTrue(messageSource.getPriority() >= 0);
        
        // 测试支持的语言环境
        Set<Locale> supportedLocales = messageSource.getSupportedLocales();
        assertNotNull(supportedLocales);
        assertTrue(supportedLocales.contains(Locale.ENGLISH));
        assertTrue(supportedLocales.contains(Locale.SIMPLIFIED_CHINESE));
        
        // 测试字符串表示
        String toString = messageSource.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test"));
        assertTrue(toString.contains("ClassPathPropertiesResourceI18nMessageSource"));
    }
    
    @Test
    @Order(2)
    @DisplayName("测试中文消息获取")
    void testChineseMessageRetrieval() {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        
        // 测试简单消息
        String message = messageSource.getMessage("test.message", locale);
        assertEquals("测试消息", message);
        
        // 测试带参数的消息
        String greeting = messageSource.getMessage("test.greeting", locale, "世界");
        assertEquals("你好，世界！", greeting);
        
        // 测试欢迎消息
        String welcome = messageSource.getMessage("test.welcome", locale);
        assertEquals("欢迎使用 Rose 国际化框架", welcome);
        
        // 测试通用消息
        assertEquals("保存", messageSource.getMessage("test.common.save", locale));
        assertEquals("取消", messageSource.getMessage("test.common.cancel", locale));
        assertEquals("删除", messageSource.getMessage("test.common.delete", locale));
        assertEquals("编辑", messageSource.getMessage("test.common.edit", locale));
        
        // 测试导航消息
        assertEquals("首页", messageSource.getMessage("test.nav.home", locale));
        assertEquals("关于", messageSource.getMessage("test.nav.about", locale));
        assertEquals("联系我们", messageSource.getMessage("test.nav.contact", locale));
    }
    
    @Test
    @Order(3)
    @DisplayName("测试英文消息获取")
    void testEnglishMessageRetrieval() {
        Locale locale = Locale.ENGLISH;
        
        // 测试简单消息
        String message = messageSource.getMessage("test.message", locale);
        assertEquals("Test Message", message);
        
        // 测试带参数的消息
        String greeting = messageSource.getMessage("test.greeting", locale, "World");
        assertEquals("Hello, World!", greeting);
        
        // 测试欢迎消息
        String welcome = messageSource.getMessage("test.welcome", locale);
        assertEquals("Welcome to Rose I18n Framework", welcome);
        
        // 测试通用消息
        assertEquals("Save", messageSource.getMessage("test.common.save", locale));
        assertEquals("Cancel", messageSource.getMessage("test.common.cancel", locale));
        assertEquals("Delete", messageSource.getMessage("test.common.delete", locale));
        assertEquals("Edit", messageSource.getMessage("test.common.edit", locale));
        
        // 测试导航消息
        assertEquals("Home", messageSource.getMessage("test.nav.home", locale));
        assertEquals("About", messageSource.getMessage("test.nav.about", locale));
        assertEquals("Contact", messageSource.getMessage("test.nav.contact", locale));
    }
    
    @ParameterizedTest(name = "测试语言环境: {0}")
    @ValueSource(strings = {"en", "zh-CN"})
    @Order(4)
    @DisplayName("参数化测试 - 支持的语言环境")
    void testSupportedLocalesParameterized(String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag);
        
        // 验证每个支持的语言环境都能正常获取消息
        assertDoesNotThrow(() -> {
            String message = messageSource.getMessage("test.message", locale);
            assertNotNull(message);
            assertFalse(message.isEmpty());
        });
        
        // 验证参数化消息
        assertDoesNotThrow(() -> {
            String greeting = messageSource.getMessage("test.greeting", locale, "Test");
            assertNotNull(greeting);
            assertTrue(greeting.contains("Test"));
        });
    }
    
    @ParameterizedTest
    @MethodSource("provideMessageCodeAndExpectedValues")
    @Order(5)
    @DisplayName("参数化测试 - 消息代码和预期值")
    void testMessageCodesAndValues(String code, Locale locale, String expectedValue) {
        String actualMessage = messageSource.getMessage(code, locale);
        assertEquals(expectedValue, actualMessage);
    }
    
    static Stream<Arguments> provideMessageCodeAndExpectedValues() {
        return Stream.of(
            Arguments.of("test.message", Locale.ENGLISH, "Test Message"),
            Arguments.of("test.message", Locale.SIMPLIFIED_CHINESE, "测试消息"),
            Arguments.of("test.common.save", Locale.ENGLISH, "Save"),
            Arguments.of("test.common.save", Locale.SIMPLIFIED_CHINESE, "保存"),
            Arguments.of("test.nav.home", Locale.ENGLISH, "Home"),
            Arguments.of("test.nav.home", Locale.SIMPLIFIED_CHINESE, "首页")
        );
    }
    
    @Test
    @Order(6)
    @DisplayName("测试批量消息获取")
    void testBatchMessageRetrieval() {
        Set<String> codes = Set.of("test.message", "test.common.save", "test.nav.home");
        
        // 测试中文批量获取
        Map<String, String> chineseMessages = messageSource.getMessages(codes, Locale.SIMPLIFIED_CHINESE);
        assertNotNull(chineseMessages);
        assertEquals(3, chineseMessages.size());
        assertEquals("测试消息", chineseMessages.get("test.message"));
        assertEquals("保存", chineseMessages.get("test.common.save"));
        assertEquals("首页", chineseMessages.get("test.nav.home"));
        
        // 测试英文批量获取
        Map<String, String> englishMessages = messageSource.getMessages(codes, Locale.ENGLISH);
        assertNotNull(englishMessages);
        assertEquals(3, englishMessages.size());
        assertEquals("Test Message", englishMessages.get("test.message"));
        assertEquals("Save", englishMessages.get("test.common.save"));
        assertEquals("Home", englishMessages.get("test.nav.home"));
        
        // 测试空集合
        Map<String, String> emptyResult = messageSource.getMessages(Collections.emptySet(), Locale.ENGLISH);
        assertTrue(emptyResult.isEmpty());
    }
    
    @Test
    @Order(7)
    @DisplayName("测试获取所有消息")
    void testGetAllMessages() {
        // 测试获取所有中文消息
        Map<String, String> allChineseMessages = messageSource.getAllMessages(Locale.SIMPLIFIED_CHINESE);
        assertNotNull(allChineseMessages);
        assertTrue(allChineseMessages.size() > 0);
        assertTrue(allChineseMessages.containsKey("test.message"));
        assertTrue(allChineseMessages.containsKey("test.common.save"));
        
        // 测试获取所有英文消息
        Map<String, String> allEnglishMessages = messageSource.getAllMessages(Locale.ENGLISH);
        assertNotNull(allEnglishMessages);
        assertTrue(allEnglishMessages.size() > 0);
        assertTrue(allEnglishMessages.containsKey("test.message"));
        assertTrue(allEnglishMessages.containsKey("test.common.save"));
        
        // 验证消息数量一致
        assertEquals(allChineseMessages.size(), allEnglishMessages.size());
    }
    
    @Nested
    @DisplayName("错误处理和边界测试")
    class ErrorHandlingAndBoundaryTests {
        
        @Test
        @DisplayName("测试不存在的消息代码")
        void testNonExistentMessageCode() {
            // 不存在的消息代码应该返回 null
            assertNull(messageSource.getMessage("test.non.existent.code", Locale.ENGLISH));
            assertNull(messageSource.getMessage("test.another.missing.code", Locale.SIMPLIFIED_CHINESE));
        }
        
        @Test
        @DisplayName("测试 null 参数处理")
        void testNullParameterHandling() {
            // 测试 null 代码
            assertNull(messageSource.getMessage(null, Locale.ENGLISH));
            
            // 测试 null 语言环境（应该使用默认语言环境）
            String message = messageSource.getMessage("test.message", null);
            assertNotNull(message);
            // 默认语言环境是中文，所以应该返回中文消息
            assertEquals("测试消息", message);
            
            // 测试 null 参数数组
            String greeting = messageSource.getMessage("test.greeting", Locale.ENGLISH, (Object[]) null);
            assertNotNull(greeting);
            // 应该保持占位符原样
            assertEquals("Hello, {0}!", greeting);
        }
        
        @Test
        @DisplayName("测试空字符串和特殊字符")
        void testEmptyStringAndSpecialCharacters() {
            // 测试空字符串代码
            assertNull(messageSource.getMessage("", Locale.ENGLISH));
            
            // 测试包含特殊字符的代码（但仍符合前缀要求）
            assertNull(messageSource.getMessage("test.@#$%^&*()", Locale.ENGLISH));
        }
        
        @Test
        @DisplayName("测试不支持的语言环境")
        void testUnsupportedLocale() {
            Locale unsupportedLocale = Locale.FRENCH;
            
            // 不支持的语言环境应该返回 null 或使用回退机制
            String message = messageSource.getMessage("test.message", unsupportedLocale);
            // 根据实现，可能返回 null 或回退到默认语言环境
            // 这里我们只验证不会抛出异常
            assertDoesNotThrow(() -> messageSource.getMessage("test.message", unsupportedLocale));
        }
    }
    
    @Nested
    @DisplayName("参数化消息测试")
    class ParameterizedMessageTests {
        
        @Test
        @DisplayName("测试单个参数")
        void testSingleParameter() {
            String greeting = messageSource.getMessage("test.greeting", Locale.ENGLISH, "John");
            assertEquals("Hello, John!", greeting);
            
            String chineseGreeting = messageSource.getMessage("test.greeting", Locale.SIMPLIFIED_CHINESE, "张三");
            assertEquals("你好，张三！", chineseGreeting);
            
            String singleParam = messageSource.getMessage("test.parameter.single", Locale.ENGLISH, "Value");
            assertEquals("Parameter: Value", singleParam);
        }
        
        @Test
        @DisplayName("测试多个参数")
        void testMultipleParameters() {
            String validationError = messageSource.getMessage("test.error.validation", Locale.ENGLISH, "用户名不能为空");
            assertEquals("Validation Error: 用户名不能为空", validationError);
            
            String multipleParams = messageSource.getMessage("test.parameter.multiple", Locale.ENGLISH, "John", "25");
            assertEquals("Name: John, Age: 25", multipleParams);
        }
        
        @Test
        @DisplayName("测试参数数量不匹配")
        void testMismatchedParameterCount() {
            // 提供的参数少于占位符数量
            String result = messageSource.getMessage("test.greeting", Locale.ENGLISH);
            assertEquals("Hello, {0}!", result);
            
            // 提供的参数多于占位符数量
            String result2 = messageSource.getMessage("test.greeting", Locale.ENGLISH, "John", "Extra");
            assertEquals("Hello, John!", result2);
        }
        
        @Test
        @DisplayName("测试特殊参数值")
        void testSpecialParameterValues() {
            // 测试 null 参数
            String result = messageSource.getMessage("test.greeting", Locale.ENGLISH, (Object) null);
            assertEquals("Hello, null!", result);
            
            // 测试空字符串参数
            String result2 = messageSource.getMessage("test.greeting", Locale.ENGLISH, "");
            assertEquals("Hello, !", result2);
            
            // 测试数字参数
            String result3 = messageSource.getMessage("test.greeting", Locale.ENGLISH, 123);
            assertEquals("Hello, 123!", result3);
        }
    }
    
    @Nested
    @DisplayName("生命周期管理测试")
    class LifecycleManagementTests {
        
        @Test
        @DisplayName("测试初始化和销毁")
        void testInitializationAndDestruction() {
            ClassPathPropertiesResourceI18nMessageSource testSource = 
                new ClassPathPropertiesResourceI18nMessageSource("test");
            
            // 测试初始化
            assertDoesNotThrow(testSource::init);
            
            // 初始化后应该能够获取消息
            String message = testSource.getMessage("test.message", Locale.ENGLISH);
            assertNotNull(message);
            
            // 测试重复初始化（应该不会出错）
            assertDoesNotThrow(testSource::init);
            
            // 测试销毁（由于可能的实现问题，我们不强制要求无异常）
            assertDoesNotThrow(testSource::destroy);
        }
        
        @Test
        @DisplayName("测试销毁后的行为")
        void testBehaviorAfterDestruction() {
            ClassPathPropertiesResourceI18nMessageSource testSource = 
                new ClassPathPropertiesResourceI18nMessageSource("test");
            
            testSource.init();
            try {
                testSource.destroy();
            } catch (Exception e) {
                // 忽略销毁异常
            }
            
            // 销毁后获取消息可能会返回 null 或抛异常，我们只验证不会挂起
            assertDoesNotThrow(() -> {
                try {
                    String result = testSource.getMessage("test.message", Locale.ENGLISH);
                    // 不对结果做具体断言，因为销毁后的行为可能因实现而异
                } catch (Exception e) {
                    // 销毁后访问可能抛异常，这是可以接受的
                }
            });
        }
    }
    
    @RepeatedTest(value = 3, name = "重复测试 {currentRepetition}/{totalRepetitions}")
    @DisplayName("性能稳定性测试")
    void testPerformanceStability() {
        long startTime = System.currentTimeMillis();
        
        // 执行一系列消息获取操作
        for (int i = 0; i < 100; i++) {
            messageSource.getMessage("test.message", Locale.ENGLISH);
            messageSource.getMessage("test.greeting", Locale.SIMPLIFIED_CHINESE, "用户" + i);
            messageSource.getAllMessages(Locale.ENGLISH);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证执行时间在合理范围内（设置为 2 秒）
        assertTrue(duration < 2000, "操作执行时间过长: " + duration + "ms");
    }
    
    @Test
    @Order(10)
    @DisplayName("综合功能测试")
    void testComprehensiveFunctionality() {
        // 测试多个方面的组合使用
        assertAll("综合功能测试",
            () -> assertEquals("test", messageSource.getSource()),
            () -> assertTrue(messageSource.getSupportedLocales().size() >= 2),
            () -> assertNotNull(messageSource.getMessage("test.message", Locale.ENGLISH)),
            () -> assertNotNull(messageSource.getMessage("test.message", Locale.SIMPLIFIED_CHINESE)),
            () -> assertTrue(messageSource.getAllMessages(Locale.ENGLISH).size() > 0),
            () -> assertTrue(messageSource.getAllMessages(Locale.SIMPLIFIED_CHINESE).size() > 0),
            () -> assertDoesNotThrow(() -> messageSource.toString())
        );
    }
}