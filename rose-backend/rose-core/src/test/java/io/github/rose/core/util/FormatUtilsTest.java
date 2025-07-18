package io.github.rose.core.util;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FormatUtils 测试类
 *
 * @author rose
 * @since 0.0.1
 */
class FormatUtilsTest {

    @Test
    void testReplacePlaceholdersWithLocale() {
        // 测试数字格式化
        String template = "价格: {}, 数量: {}";
        String result = FormatUtils.replacePlaceholders(template, Locale.US, 1234.56, 1000);
        assertEquals("价格: 1,234.56, 数量: 1,000", result);

        // 测试中文数字格式化
        String resultCN = FormatUtils.replacePlaceholders(template, Locale.CHINA, 1234.56, 1000);
        assertEquals("价格: 1,234.56, 数量: 1,000", resultCN);
    }

    @Test
    void testReplaceNamedParametersWithLocale() {
        Map<String, Object> params = new HashMap<>();
        params.put("price", 1234.56);
        params.put("quantity", 1000);
        params.put("date", new Date(1234567890000L)); // 2009-02-13 23:31:30

        String template = "价格: {price}, 数量: {quantity}, 日期: {date}";
        String result = FormatUtils.replaceNamedParameters(template, params, Locale.US);

        assertTrue(result.contains("价格: 1,234.56"));
        assertTrue(result.contains("数量: 1,000"));
        assertTrue(result.contains("日期: 2009-02-13"));
    }

    @Test
    void testReplaceIndexedParametersWithLocale() {
        String template = "价格: {0}, 数量: {1}, 日期: {2}";
        Date date = new Date(1234567890000L); // 2009-02-13 23:31:30

        String result = FormatUtils.replaceIndexedParameters(template, Locale.US, 1234.56, 1000, date);

        assertTrue(result.contains("价格: 1,234.56"));
        assertTrue(result.contains("数量: 1,000"));
        assertTrue(result.contains("日期: 2009-02-13"));
    }

    @Test
    void testFormatValueWithLocale() {
        // 测试数字格式化
        assertEquals("1,234.56", FormatUtils.formatValue(1234.56, Locale.US));
        assertEquals("1,234.56", FormatUtils.formatValue(1234.56, Locale.CHINA));

        // 测试日期格式化
        Date date = new Date(1234567890000L); // 2009-02-13 23:31:30
        String dateResult = FormatUtils.formatValue(date, Locale.US);
        assertTrue(dateResult.contains("2009-02-13"));

        // 测试其他类型
        assertEquals("true", FormatUtils.formatValue(true, Locale.US));
        assertEquals("hello", FormatUtils.formatValue("hello", Locale.US));
        assertEquals("A", FormatUtils.formatValue('A', Locale.US));
    }

    @Test
    void testFormatWithLocale() {
        // 测试 Map 参数
        Map<String, Object> params = new HashMap<>();
        params.put("price", 1234.56);
        params.put("quantity", 1000);

        String template = "价格: {price}, 数量: {quantity}";
        String result = FormatUtils.format(template, Locale.US, params);

        assertTrue(result.contains("价格: 1,234.56"));
        assertTrue(result.contains("数量: 1,000"));

        // 测试数组参数
        String template2 = "价格: {}, 数量: {}";
        Object[] args = {1234.56, 1000};
        String result2 = FormatUtils.format(template2, Locale.US, args);

        assertTrue(result2.contains("价格: 1,234.56"));
        assertTrue(result2.contains("数量: 1,000"));
    }

    @Test
    void testBackwardCompatibility() {
        // 测试向后兼容性 - 不带 Locale 参数的方法应该使用默认 Locale
        String template = "价格: {}, 数量: {}";
        String result = FormatUtils.replacePlaceholders(template, 1234.56, 1000);

        // 应该使用系统默认 Locale 进行格式化
        assertNotNull(result);
        assertTrue(result.contains("价格:"));
        assertTrue(result.contains("数量:"));
    }

    @Test
    void testNullAndEmptyHandling() {
        // 测试空模板
        assertEquals(null, FormatUtils.replacePlaceholders(null, Locale.US, "test"));
        assertEquals("", FormatUtils.replacePlaceholders("", Locale.US, "test"));
        assertEquals("   ", FormatUtils.replacePlaceholders("   ", Locale.US, "test"));

        // 测试空参数
        assertEquals("test", FormatUtils.replacePlaceholders("test", Locale.US));
        assertEquals("test", FormatUtils.replacePlaceholders("test", Locale.US, (Object[]) null));
    }

    @Test
    void testDifferentNumberTypes() {
        // 测试不同类型的数字
        assertEquals("1,234", FormatUtils.formatValue(1234, Locale.US));
        assertEquals("1,234.56", FormatUtils.formatValue(1234.56f, Locale.US));
        assertEquals("1,234.56", FormatUtils.formatValue(1234.56d, Locale.US));
        assertEquals("1,234", FormatUtils.formatValue(1234L, Locale.US));
    }
} 