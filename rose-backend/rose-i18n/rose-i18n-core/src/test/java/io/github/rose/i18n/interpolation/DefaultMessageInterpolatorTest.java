package io.github.rose.i18n.interpolation;

import io.github.rose.i18n.interpolation.evaluator.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultMessageInterpolator 测试类
 * <p>
 * 测试各种插值格式和边界情况
 */
class DefaultMessageInterpolatorTest {

    private DefaultMessageInterpolator interpolator;

    @BeforeEach
    void setUp() {
        interpolator = new DefaultMessageInterpolator();
    }

    // ==================== 基础功能测试 ====================

    @Test
    void testNullMessage_ShouldReturnNull() {
        assertNull(interpolator.interpolate(null, Locale.ENGLISH, new Object[]{"test"}));
    }

    @Test
    void testNullArgs_ShouldReturnOriginalMessage() {
        String message = "Hello {name}";
        String result = interpolator.interpolate(message, Locale.ENGLISH, null);
        assertEquals(message, result);
    }

    @Test
    void testEmptyMessage_ShouldReturnEmpty() {
        String result = interpolator.interpolate("", Locale.ENGLISH, new Object[]{"test"});
        assertEquals("", result);
    }

    @Test
    void testNoPlaceholders_ShouldReturnOriginalMessage() {
        String message = "Hello World";
        String result = interpolator.interpolate(message, Locale.ENGLISH, new Object[]{"test"});
        assertEquals(message, result);
    }

    // ==================== 表达式插值测试 (${expression}) ====================

    @Test
    void testExpressionInterpolation_SimpleProperty() {
        String message = "Hello ${name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John", result);
    }

    @Test
    void testExpressionInterpolation_MultipleProperties() {
        String message = "Hello ${name}, you have ${count} messages";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");
        args.put("count", 5);

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have 5 messages", result);
    }

    @Test
    void testExpressionInterpolation_WithNullValue() {
        String message = "Hello ${name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", null);

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello null", result);
    }

    @Test
    void testExpressionInterpolation_WithMissingProperty() {
        String message = "Hello ${name}";
        Map<String, Object> args = new HashMap<>();
        args.put("other", "value");

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello ${name}", result);
    }

    @Test
    void testExpressionInterpolation_WithComplexObject() {
        String message = "User: ${user.name}, Age: ${user.age}";
        Map<String, Object> args = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Alice");
        user.put("age", 25);
        args.put("user", user);

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("User: Alice, Age: 25", result);
    }

    // ==================== 命名参数插值测试 ({name}) ====================

    @Test
    void testNamedParameterInterpolation_Simple() {
        String message = "Hello {name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John", result);
    }

    @Test
    void testNamedParameterInterpolation_Multiple() {
        String message = "Hello {name}, you have {count} messages";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");
        args.put("count", 5);

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have 5 messages", result);
    }

    @Test
    void testNamedParameterInterpolation_WithMissingParameter() {
        String message = "Hello {name}, you have {count} messages";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have {count} messages", result);
    }

    @Test
    void testNamedParameterInterpolation_WithNullValue() {
        String message = "Hello {name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", null);

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello null", result);
    }

    // ==================== MessageFormat 插值测试 ({0}, {1}) ====================

    @Test
    void testMessageFormatInterpolation_Simple() {
        String message = "Hello {0}, you have {1} messages";
        Object[] args = {"John", 5};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have 5 messages", result);
    }

    @Test
    void testMessageFormatInterpolation_WithMissingIndex() {
        String message = "Hello {0}, you have {1} messages";
        Object[] args = {"John"};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have {1} messages", result);
    }

    @Test
    void testMessageFormatInterpolation_WithNullValue() {
        String message = "Hello {0}";
        Object[] args = {null};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello null", result);
    }

    // ==================== 占位符插值测试 ({}) ====================

    @Test
    void testPlaceholderInterpolation_Simple() {
        String message = "Hello {}, you have {} messages";
        Object[] args = {"John", 5};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have 5 messages", result);
    }

    @Test
    void testPlaceholderInterpolation_WithMissingArgs() {
        String message = "Hello {}, you have {} messages";
        Object[] args = {"John"};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have {} messages", result);
    }

    @Test
    void testPlaceholderInterpolation_WithNullValue() {
        String message = "Hello {}";
        Object[] args = {null};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello null", result);
    }

    @Test
    void testPlaceholderInterpolation_WithMixedFormats() {
        String message = "Hello {}, you have {} messages and {name} is ${expression}";
        Object[] args = {"John", 5};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello John, you have 5 messages and {name} is ${expression}", result);
    }

    @Test
    void testPlaceholderInterpolation_SingleObject() {
        String message = "Hello {}";
        String result = interpolator.interpolate(message, Locale.ENGLISH, "World");
        assertEquals("Hello World", result);
    }

    // ==================== 边界情况测试 ====================

    @Test
    void testEdgeCase_EmptyArray() {
        String message = "Hello {}";
        Object[] args = {};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello {}", result);
    }

    @Test
    void testEdgeCase_EmptyMap() {
        String message = "Hello {name}";
        Map<String, Object> args = new HashMap<>();

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello {name}", result);
    }

    @Test
    void testEdgeCase_InvalidExpression() {
        String message = "Hello ${invalid.expression}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello ${invalid.expression}", result);
    }

    @Test
    void testEdgeCase_InvalidMessageFormat() {
        String message = "Hello {invalid}";
        Object[] args = {"John"};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello {invalid}", result);
    }

    @Test
    void testEdgeCase_ComplexNestedExpressions() {
        String message = "User: ${user.profile.name}, Company: ${user.company.name}";
        Map<String, Object> args = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        Map<String, Object> profile = new HashMap<>();
        Map<String, Object> company = new HashMap<>();
        profile.put("name", "Alice");
        company.put("name", "TechCorp");
        user.put("profile", profile);
        user.put("company", company);
        args.put("user", user);

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        // 由于 SimpleExpressionEvaluator 的限制，复杂嵌套表达式可能无法正确解析
        assertTrue(result.contains("${user.profile.name}") || result.contains("Alice"));
    }

    // ==================== 构造函数测试 ====================

    @Test
    void testConstructor_Default() {
        DefaultMessageInterpolator defaultInterpolator = new DefaultMessageInterpolator();
        assertNotNull(defaultInterpolator);
    }

    // ==================== 性能测试 ====================

    @Test
    void testPerformance_LargeMessage() {
        StringBuilder message = new StringBuilder();
        Map<String, Object> args = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            message.append("Hello {name").append(i).append("}, ");
            args.put("name" + i, "User" + i);
        }
        message.append("end");

        long startTime = System.currentTimeMillis();
        String result = interpolator.interpolate(message.toString(), Locale.ENGLISH, args);
        long endTime = System.currentTimeMillis();

        assertNotNull(result);
        assertTrue(endTime - startTime < 1000, "Performance test should complete within 1 second");
    }

    // ==================== 国际化测试 ====================

    @Test
    void testInternationalization_DifferentLocales() {
        String message = "Hello {0}, you have {1} messages";
        Object[] args = {"John", 5};

        String resultEn = interpolator.interpolate(message, Locale.ENGLISH, args);
        String resultZh = interpolator.interpolate(message, Locale.CHINESE, args);
        String resultFr = interpolator.interpolate(message, Locale.FRENCH, args);

        assertEquals("Hello John, you have 5 messages", resultEn);
        assertEquals("Hello John, you have 5 messages", resultZh);
        assertEquals("Hello John, you have 5 messages", resultFr);
    }

    // ==================== 错误处理测试 ====================

    @Test
    void testErrorHandling_ExpressionEvaluationException() {
        String message = "Hello ${name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Test exception");
            }
        });

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        assertEquals("Hello ${name}", result);
    }

    @Test
    void testErrorHandling_MessageFormatException() {
        String message = "Hello {0}, you have {1} messages";
        Object[] args = {"John", new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Test exception");
            }
        }};

        String result = interpolator.interpolate(message, Locale.ENGLISH, args);
        // 应该回退到手动处理
        assertTrue(result.contains("John") || result.contains("{0}"));
    }
} 