package io.github.rose.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FormatUtils 测试类
 * 测试字符串格式化和占位符替换功能
 */
class FormatUtilsTest {

    @Test
    void testReplacePlaceholdersBasic() {
        // 基本占位符替换
        String result = FormatUtils.replacePlaceholders("Hello {}", "World");
        assertEquals("Hello World", result);

        // 多个占位符
        result = FormatUtils.replacePlaceholders("用户 {} 年龄 {} 岁", "张三", 25);
        assertEquals("用户 张三 年龄 25 岁", result);

        // 无占位符
        result = FormatUtils.replacePlaceholders("Hello World");
        assertEquals("Hello World", result);

        // 空字符串
        result = FormatUtils.replacePlaceholders("", "test");
        assertEquals("", result);

        // null模板
        result = FormatUtils.replacePlaceholders(null, "test");
        assertNull(result);
    }

    @Test
    void testReplacePlaceholdersWithNullArgs() {
        // null参数
        String result = FormatUtils.replacePlaceholders("Hello {}", (Object) null);
        assertEquals("Hello null", result);

        // 空参数数组
        result = FormatUtils.replacePlaceholders("Hello {}");
        assertEquals("Hello {}", result); // 没有参数时保持原样

        // 参数不足
        result = FormatUtils.replacePlaceholders("Hello {} {}", "World");
        assertEquals("Hello World {}", result); // 未匹配的占位符保持原样
    }

    @Test
    void testReplacePlaceholdersWithDifferentTypes() {
        // 不同类型的参数
        String result = FormatUtils.replacePlaceholders(
            "字符串: {}, 数字: {}, 布尔: {}, 小数: {}",
            "测试", 42, true, 3.14
        );
        assertEquals("字符串: 测试, 数字: 42, 布尔: true, 小数: 3.14", result);
    }

    @Test
    void testReplaceCustomPlaceholder() {
        // 自定义占位符
        String result = FormatUtils.replaceCustomPlaceholder("Hello %s", "%s", "World");
        assertEquals("Hello World", result);

        // 多个自定义占位符
        result = FormatUtils.replaceCustomPlaceholder("用户 %s 年龄 %s 岁", "%s", "李四", 30);
        assertEquals("用户 李四 年龄 30 岁", result);

        // 不同的占位符
        result = FormatUtils.replaceCustomPlaceholder("Hello ${name}", "${name}", "张三");
        assertEquals("Hello 张三", result);
    }

    @Test
    void testReplaceNamedPlaceholders() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "王五");
        params.put("age", 28);
        params.put("city", "北京");

        // 命名占位符
        String result = FormatUtils.replaceNamedParameters(
            "用户 {name} 年龄 {age} 岁，来自 {city}",
            params
        );
        assertEquals("用户 王五 年龄 28 岁，来自 北京", result);

        // 部分匹配
        params.clear();
        params.put("name", "赵六");
        result = FormatUtils.replaceNamedParameters(
            "用户 {name} 年龄 {age} 岁",
            params
        );
        assertEquals("用户 赵六 年龄 {age} 岁", result); // 未匹配的保持原样
    }

    @Test
    void testReplaceIndexedPlaceholders() {
        // 索引占位符
        String result = FormatUtils.replaceIndexedParameters(
            "参数0: {0}, 参数1: {1}, 参数0再次: {0}",
            "第一个", "第二个"
        );
        assertEquals("参数0: 第一个, 参数1: 第二个, 参数0再次: 第一个", result);

        // 索引超出范围
        result = FormatUtils.replaceIndexedParameters(
            "参数0: {0}, 参数1: {1}, 参数2: {2}",
            "第一个", "第二个"
        );
        assertEquals("参数0: 第一个, 参数1: 第二个, 参数2: {2}", result);
    }

    @Test
    void testFormatWithLocale() {
        // 测试本地化格式化
        Locale chineseLocale = Locale.SIMPLIFIED_CHINESE;
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");

        String result = FormatUtils.replacePlaceholders(
            "数字: {}, 小数: {}",
            chineseLocale,
            timeZone,
            1234,
            3.14159
        );
        assertNotNull(result);
        assertTrue(result.contains("数字:"));
        assertTrue(result.contains("小数:"));
    }

    @ParameterizedTest
    @MethodSource("providePlaceholderTestCases")
    void testPlaceholderReplacement(String template, Object[] args, String expected) {
        String result = FormatUtils.replacePlaceholders(template, args);
        assertEquals(expected, result);
    }

    static Stream<Arguments> providePlaceholderTestCases() {
        return Stream.of(
            Arguments.of("简单测试: {}", new Object[]{"成功"}, "简单测试: 成功"),
            Arguments.of("多参数: {} + {} = {}", new Object[]{1, 2, 3}, "多参数: 1 + 2 = 3"),
            Arguments.of("无占位符", new Object[]{"忽略"}, "无占位符"),
            Arguments.of("空参数: {}", new Object[]{""}, "空参数: "),
            Arguments.of("特殊字符: {} & {}", new Object[]{"<test>", "\"quote\""}, "特殊字符: <test> & \"quote\"")
        );
    }

    @Test
    void testEdgeCases() {
        // 边界情况测试
        
        // 连续占位符
        String result = FormatUtils.replacePlaceholders("{}{}{}", "A", "B", "C");
        assertEquals("ABC", result);

        // 占位符在开头和结尾
        result = FormatUtils.replacePlaceholders("{}中间{}", "开始", "结束");
        assertEquals("开始中间结束", result);

        // 大量占位符
        Object[] manyArgs = new Object[10];
        StringBuilder template = new StringBuilder();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            manyArgs[i] = "参数" + i;
            template.append("{}");
            expected.append("参数").append(i);
            if (i < 9) {
                template.append("-");
                expected.append("-");
            }
        }
        result = FormatUtils.replacePlaceholders(template.toString(), manyArgs);
        assertEquals(expected.toString(), result);
    }

    @Test
    void testSpecialCharactersInTemplate() {
        // 模板中包含特殊字符
        String result = FormatUtils.replacePlaceholders(
            "JSON: {\"key\": \"{}\"}", 
            "value"
        );
        assertEquals("JSON: {\"key\": \"value\"}", result);

        // 包含换行符
        result = FormatUtils.replacePlaceholders(
            "第一行: {}\n第二行: {}", 
            "内容1", "内容2"
        );
        assertEquals("第一行: 内容1\n第二行: 内容2", result);
    }

    @Test
    void testPerformance() {
        // 性能测试 - 确保大量替换不会有性能问题
        String template = "用户 {} 在 {} 执行了 {} 操作，结果: {}";
        Object[] args = {"测试用户", "2023-12-01", "登录", "成功"};
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            FormatUtils.replacePlaceholders(template, args);
        }
        long endTime = System.currentTimeMillis();
        
        // 1000次操作应该在合理时间内完成（比如1秒）
        assertTrue(endTime - startTime < 1000, "格式化操作耗时过长");
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // 线程安全测试
        String template = "线程 {} 执行第 {} 次操作";
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String result = FormatUtils.replacePlaceholders(template, threadId, j);
                    assertEquals("线程 " + threadId + " 执行第 " + j + " 次操作", result);
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    void testComplexObjects() {
        // 复杂对象测试
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("123.45");
        
        String result = FormatUtils.replacePlaceholders(
            "时间: {}, 金额: {}, 对象: {}",
            now, amount, new TestObject("测试", 42)
        );
        
        assertNotNull(result);
        assertTrue(result.contains("时间:"));
        assertTrue(result.contains("金额:"));
        assertTrue(result.contains("对象:"));
    }

    // 测试用的简单对象
    static class TestObject {
        private final String name;
        private final int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', value=" + value + "}";
        }
    }
}
