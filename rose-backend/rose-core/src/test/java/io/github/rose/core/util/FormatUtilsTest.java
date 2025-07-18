package io.github.rose.core.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FormatUtils 单元测试
 *
 * <p>测试覆盖范围：</p>
 * <ul>
 *   <li>变量替换功能</li>
 *   <li>占位符替换功能</li>
 *   <li>命名参数替换功能</li>
 *   <li>索引参数替换功能</li>
 *   <li>智能格式化功能</li>
 *   <li>边界情况和错误处理</li>
 *   <li>性能相关方法</li>
 * </ul>
 */
class FormatUtilsTest {

    @Test
    void testReplaceVariables_WithBlankTemplate() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");

        String result = FormatUtils.replaceNamedParameters("", variables);
        assertEquals("", result);
    }

    @Test
    void testReplaceVariables_WithEmptyKey() {
        String template = "Hello {name}, welcome to {}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("", "Rose");
    }

    // ==================== 占位符替换测试 ====================

    @Test
    void testReplacePlaceholders_Simple() {
        String template = "Hello {}, welcome to {}";
        String result = FormatUtils.replacePlaceholders(template, "John", "Rose");
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testReplacePlaceholders_WithNullValue() {
        String template = "Hello {}, your age is {}";
        String result = FormatUtils.replacePlaceholders(template, "John", null);
        assertEquals("Hello John, your age is null", result);
    }

    @Test
    void testReplacePlaceholders_WithMoreArgsThanPlaceholders() {
        String template = "Hello {}, welcome to {}";
        String result = FormatUtils.replacePlaceholders(template, "John", "Rose", "Extra");
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testReplacePlaceholders_WithFewerArgsThanPlaceholders() {
        String template = "Hello {}, welcome to {}, your age is {}";
        String result = FormatUtils.replacePlaceholders(template, "John", "Rose");
        assertEquals("Hello John, welcome to Rose, your age is {}", result);
    }

    @Test
    void testReplacePlaceholders_WithCustomPlaceholder() {
        String template = "Hello %s, welcome to %s";
        String result = FormatUtils.replaceCustomPlaceholder(template, "%s", "John", "Rose");
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testReplacePlaceholders_WithEmptyTemplate() {
        String result = FormatUtils.replacePlaceholders("", "John", "Rose");
        assertEquals("", result);
    }

    @Test
    void testReplacePlaceholders_WithNullArgs() {
        String template = "Hello {}, welcome to {}";
        String result = FormatUtils.replacePlaceholders(template, (Object[]) null);
        assertEquals(template, result);
    }

    @Test
    void testReplacePlaceholders_WithEmptyArgs() {
        String template = "Hello {}, welcome to {}";
        String result = FormatUtils.replacePlaceholders(template);
        assertEquals(template, result);
    }

    // ==================== 命名参数替换测试 ====================

    @Test
    void testReplaceNamedParameters_Simple() {
        String template = "Hello {name}, welcome to {site}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("site", "Rose");

        String result = FormatUtils.replaceNamedParameters(template, variables);
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testReplaceNamedParameters_WithMissingParameter() {
        String template = "Hello {name}, your age is {age}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");

        String result = FormatUtils.replaceNamedParameters(template, variables);
        assertEquals("Hello John, your age is {age}", result);
    }

    @Test
    void testReplaceNamedParameters_WithSpecialCharacters() {
        String template = "Hello {user_name}, your email is {email}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("user_name", "john_doe");
        variables.put("email", "john@example.com");

        String result = FormatUtils.replaceNamedParameters(template, variables);
        assertEquals("Hello john_doe, your email is john@example.com", result);
    }

    @Test
    void testReplaceNamedParameters_WithRegexSpecialCharacters() {
        String template = "Hello {name}, your path is {path}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("path", "C:\\Users\\John\\Documents");

        String result = FormatUtils.replaceNamedParameters(template, variables);
        assertEquals("Hello John, your path is C:\\Users\\John\\Documents", result);
    }

    // ==================== 索引参数替换测试 ====================

    @Test
    void testReplaceIndexedParameters_Simple() {
        String template = "Hello {0}, welcome to {1}";
        String result = FormatUtils.replaceIndexedParameters(template, "John", "Rose");
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testReplaceIndexedParameters_WithOutOfRangeIndex() {
        String template = "Hello {0}, your age is {5}";
        String result = FormatUtils.replaceIndexedParameters(template, "John", 25);
        assertEquals("Hello John, your age is {5}", result);
    }

    @Test
    void testReplaceIndexedParameters_WithNonNumericIndex() {
        String template = "Hello {0}, welcome to {abc}";
        String result = FormatUtils.replaceIndexedParameters(template, "John", "Rose");
        assertEquals("Hello John, welcome to {abc}", result);
    }

    @Test
    void testReplaceIndexedParameters_WithNegativeIndex() {
        String template = "Hello {0}, welcome to {-1}";
        String result = FormatUtils.replaceIndexedParameters(template, "John", "Rose");
        assertEquals("Hello John, welcome to {-1}", result);
    }

    // ==================== 智能格式化测试 ====================

    @Test
    void testFormat_WithMap() {
        String template = "Hello {name}, welcome to {site}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("site", "Rose");

        String result = FormatUtils.format(template, variables);
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testFormat_WithArray() {
        String template = "Hello {}, welcome to {}";
        Object[] args = {"John", "Rose"};

        String result = FormatUtils.format(template, args);
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testFormat_WithSingleObject() {
        String template = "Hello {}";
        String result = FormatUtils.format(template, "John");
        assertEquals("Hello John", result);
    }

    @Test
    void testFormat_WithIndexedParameters() {
        String template = "Hello {0}, welcome to {1}";
        Object[] args = {"John", "Rose"};

        String result = FormatUtils.format(template, args);
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testFormat_WithNoPlaceholders() {
        String template = "Hello World";
        String result = FormatUtils.format(template, "John");
        assertEquals("Hello World", result);
    }

    // ==================== 值格式化测试 ====================

    @Test
    void testFormatValue_WithString() {
        assertEquals("Hello", FormatUtils.formatValue("Hello"));
    }

    @Test
    void testFormatValue_WithNumber() {
        assertEquals("42", FormatUtils.formatValue(42));
        assertEquals("3.14", FormatUtils.formatValue(3.14));
    }

    @Test
    void testFormatValue_WithBoolean() {
        assertEquals("true", FormatUtils.formatValue(true));
        assertEquals("false", FormatUtils.formatValue(false));
    }

    @Test
    void testFormatValue_WithCharacter() {
        assertEquals("A", FormatUtils.formatValue('A'));
    }

    @Test
    void testFormatValue_WithNull() {
        assertEquals("null", FormatUtils.formatValue(null));
    }

    @Test
    void testFormatValue_WithCustomObject() {
        Object customObject = new Object() {
            @Override
            public String toString() {
                return "CustomObject";
            }
        };
        assertEquals("CustomObject", FormatUtils.formatValue(customObject));
    }

    // ==================== 检查方法测试 ====================

    @Test
    void testHasPlaceholders_WithPlaceholders() {
        assertTrue(FormatUtils.hasPlaceholders("Hello {}, welcome to {}"));
    }

    @Test
    void testHasPlaceholders_WithoutPlaceholders() {
        assertFalse(FormatUtils.hasPlaceholders("Hello World"));
    }

    @Test
    void testHasPlaceholders_WithNull() {
        assertFalse(FormatUtils.hasPlaceholders(null));
    }

    @Test
    void testHasPlaceholders_WithEmpty() {
        assertFalse(FormatUtils.hasPlaceholders(""));
    }

    @Test
    void testHasNamedParameters_WithNamedParameters() {
        assertTrue(FormatUtils.hasNamedParameters("Hello {name}, welcome to {site}"));
    }

    @Test
    void testHasNamedParameters_WithoutNamedParameters() {
        assertFalse(FormatUtils.hasNamedParameters("Hello World"));
    }

    @Test
    void testHasIndexedParameters_WithIndexedParameters() {
        assertTrue(FormatUtils.hasIndexedParameters("Hello {0}, welcome to {1}"));
    }

    @Test
    void testHasIndexedParameters_WithoutIndexedParameters() {
        assertFalse(FormatUtils.hasIndexedParameters("Hello World"));
    }

    // ==================== 统计方法测试 ====================

    @Test
    void testCountPlaceholders_WithMultiplePlaceholders() {
        assertEquals(3, FormatUtils.countPlaceholders("Hello {}, welcome to {}, your age is {}"));
    }

    @Test
    void testCountPlaceholders_WithNoPlaceholders() {
        assertEquals(0, FormatUtils.countPlaceholders("Hello World"));
    }

    @Test
    void testCountPlaceholders_WithNull() {
        assertEquals(0, FormatUtils.countPlaceholders(null));
    }

    @Test
    void testCountNamedParameters_WithMultipleParameters() {
        assertEquals(2, FormatUtils.countNamedParameters("Hello {name}, welcome to {site}"));
    }

    @Test
    void testCountNamedParameters_WithNoParameters() {
        assertEquals(0, FormatUtils.countNamedParameters("Hello World"));
    }

    // ==================== 边界情况测试 ====================

    @Test
    void testEdgeCase_EmptyString() {
        assertEquals("", FormatUtils.replacePlaceholders(""));
        assertEquals("", FormatUtils.replaceNamedParameters("", new HashMap<>()));
    }

    @Test
    void testEdgeCase_NullTemplate() {
        assertNull(FormatUtils.replacePlaceholders(null));
        assertNull(FormatUtils.replaceNamedParameters(null, new HashMap<>()));
    }

    @Test
    void testEdgeCase_BlankTemplate() {
        assertEquals("   ", FormatUtils.replacePlaceholders("   "));
        assertEquals("   ", FormatUtils.replaceNamedParameters("   ", new HashMap<>()));
    }

    @Test
    void testEdgeCase_SpecialCharactersInReplacement() {
        String template = "Hello {name}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John$Doe");

        String result = FormatUtils.replaceNamedParameters(template, variables);
        assertEquals("Hello John$Doe", result);
    }

    // ==================== 性能测试 ====================

    @Test
    void testPerformance_LargeTemplate() {
        StringBuilder template = new StringBuilder();
        Map<String, Object> variables = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            template.append("Hello {name").append(i).append("}, ");
            variables.put("name" + i, "value" + i);
        }

        long start = System.nanoTime();
        String result = FormatUtils.replaceNamedParameters(template.toString(), variables);
        long end = System.nanoTime();

        assertNotNull(result);
        assertTrue((end - start) < 10000000); // 应该小于10ms
    }

    // ==================== 向后兼容性测试 ====================

    @Test
    void testBackwardCompatibility_FormatPlaceholders() {
        String template = "Hello {}, welcome to {}";
        String result = FormatUtils.replacePlaceholders(template, "John", "Rose");
        assertEquals("Hello John, welcome to Rose", result);
    }

    @Test
    void testBackwardCompatibility_SmartFormat() {
        String template = "Hello {name}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");

        String result = FormatUtils.format(template, variables);
        assertEquals("Hello John", result);
    }

    @Test
    void testBackwardCompatibility_ContainsMethods() {
        assertTrue(FormatUtils.hasPlaceholders("Hello {}"));
        assertTrue(FormatUtils.hasNamedParameters("Hello {name}"));
        assertTrue(FormatUtils.hasIndexedParameters("Hello {0}"));
    }
} 