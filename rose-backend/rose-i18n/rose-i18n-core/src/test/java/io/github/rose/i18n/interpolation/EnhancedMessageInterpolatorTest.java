package io.github.rose.i18n.interpolation;

import io.github.rose.i18n.interpolation.evaluator.SimpleExpressionEvaluator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * EnhancedMessageInterpolator 测试类
 * 
 * 测试所有支持的替换格式：
 * - {} 占位符：FormatUtils.format() 风格
 * - {0}, {1} 索引：MessageFormat 风格
 * - {name} 命名参数：命名参数风格
 * - ${expression} 表达式：表达式评估风格
 */
public class EnhancedMessageInterpolatorTest {

    public static void main(String[] args) {
        EnhancedMessageInterpolatorTest test = new EnhancedMessageInterpolatorTest();
        test.runAllTests();
    }

    private EnhancedMessageInterpolator interpolator;

    public EnhancedMessageInterpolatorTest() {
        interpolator = new EnhancedMessageInterpolator();
    }

    public void runAllTests() {
        System.out.println("=== EnhancedMessageInterpolator 测试开始 ===");
        
        testSimplePlaceholderFormat();
        testMessageFormatStyle();
        testNamedParameterFormat();
        testExpressionFormat();
        testComplexExpressionFormat();
        testMixedFormats();
        testNullHandling();
        testEmptyTemplate();
        testLocaleAwareFormatting();
        testArrayAccessExpression();
        testMethodCallExpression();
        testCacheFunctionality();
        testCustomExpressionEvaluator();
        testPriorityOrder();
        
        System.out.println("=== 所有测试完成 ===");
    }

    public void testSimplePlaceholderFormat() {
        System.out.println("测试 {} 占位符格式...");
        String template = "Hello {}, you have {} messages";
        Object[] args = {"John", 5};
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "Hello John, you have 5 messages";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testMessageFormatStyle() {
        System.out.println("测试 {0}, {1} 索引格式...");
        String template = "Hello {0}, you have {1} messages";
        Object[] args = {"John", 5};
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "Hello John, you have 5 messages";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testNamedParameterFormat() {
        System.out.println("测试 {name} 命名参数格式...");
        String template = "Hello {name}, you have {count} messages";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");
        args.put("count", 5);
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "Hello John, you have 5 messages";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testExpressionFormat() {
        System.out.println("测试 ${expression} 表达式格式...");
        String template = "Hello ${user.name}, you have ${user.messageCount} messages";
        Map<String, Object> args = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John");
        user.put("messageCount", 5);
        args.put("user", user);
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "Hello John, you have 5 messages";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testComplexExpressionFormat() {
        System.out.println("测试复杂表达式...");
        String template = "User: ${user.name}, Age: ${user.age}, Active: ${user.active}";
        Map<String, Object> args = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John");
        user.put("age", 30);
        user.put("active", true);
        args.put("user", user);
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "User: John, Age: 30, Active: true";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testMixedFormats() {
        System.out.println("测试混合格式...");
        String template = "Hello ${user.name}, you have {count} messages";
        Map<String, Object> args = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John");
        args.put("user", user);
        args.put("count", 5);
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        // 由于包含表达式，会按表达式格式处理，{count} 不会被处理
        String expected = "Hello John, you have {count} messages";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testNullHandling() {
        System.out.println("测试空值处理...");
        String template = "Hello {}, you have {} messages";
        String result = interpolator.interpolate(template, null, Locale.ENGLISH);
        
        if (template.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + template + "', 实际 '" + result + "'");
        }
    }

    public void testEmptyTemplate() {
        System.out.println("测试空模板...");
        String result = interpolator.interpolate(null, new Object[]{"John"}, Locale.ENGLISH);
        
        if (result == null) {
            System.out.println("✓ 通过: null");
        } else {
            System.out.println("✗ 失败: 期望 null, 实际 '" + result + "'");
        }
    }

    public void testLocaleAwareFormatting() {
        System.out.println("测试本地化格式化...");
        String template = "Price: {0}";
        Object[] args = {1234.56};
        
        String resultEn = interpolator.interpolate(template, args, Locale.ENGLISH);
        String resultDe = interpolator.interpolate(template, args, Locale.GERMAN);
        
        System.out.println("✓ 英文格式: " + resultEn);
        System.out.println("✓ 德文格式: " + resultDe);
    }

    public void testArrayAccessExpression() {
        System.out.println("测试数组访问表达式...");
        String template = "First user: ${users[0].name}, Second user: ${users[1].name}";
        Map<String, Object> args = new HashMap<>();
        Map<String, Object>[] users = new Map[2];
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "John");
        users[0] = user1;
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "Jane");
        users[1] = user2;
        
        args.put("users", users);
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "First user: John, Second user: Jane";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testMethodCallExpression() {
        System.out.println("测试方法调用表达式...");
        
        // 调试：测试表达式分割
        String testExpression = "users.size()";
        String[] parts = testExpression.split("(?=\\[)|(?<=\\])|(?=\\.)|(?=\\()|(?<=\\))");
        System.out.println("表达式分割结果:");
        for (int i = 0; i < parts.length; i++) {
            System.out.println("  parts[" + i + "] = '" + parts[i] + "'");
        }
        
        // 只测试无参数的方法调用
        String template = "User count: ${users.size()}";
        Map<String, Object> args = new HashMap<>();
        java.util.List<String> users = java.util.Arrays.asList("John", "Jane", "Bob");
        args.put("users", users);
        String result = interpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "User count: 3";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testCacheFunctionality() {
        System.out.println("测试缓存功能...");
        String template = "Hello {}, you have {} messages";
        Object[] args = {"John", 5};
        
        // 第一次调用
        String result1 = interpolator.interpolate(template, args, Locale.ENGLISH);
        int cacheSize1 = interpolator.getCacheSize();
        
        // 第二次调用（应该使用缓存）
        String result2 = interpolator.interpolate(template, args, Locale.ENGLISH);
        int cacheSize2 = interpolator.getCacheSize();
        
        if (result1.equals(result2) && cacheSize1 == cacheSize2) {
            System.out.println("✓ 通过: 缓存工作正常");
        } else {
            System.out.println("✗ 失败: 缓存功能异常");
        }
        
        // 清空缓存
        interpolator.clearCache();
        if (interpolator.getCacheSize() == 0) {
            System.out.println("✓ 通过: 缓存清空成功");
        } else {
            System.out.println("✗ 失败: 缓存清空失败");
        }
    }

    public void testCustomExpressionEvaluator() {
        System.out.println("测试自定义表达式评估器...");
        EnhancedMessageInterpolator customInterpolator = EnhancedMessageInterpolator.create(
                new SimpleExpressionEvaluator()
        );
        
        String template = "Hello ${name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");
        String result = customInterpolator.interpolate(template, args, Locale.ENGLISH);
        
        String expected = "Hello John";
        if (expected.equals(result)) {
            System.out.println("✓ 通过: " + result);
        } else {
            System.out.println("✗ 失败: 期望 '" + expected + "', 实际 '" + result + "'");
        }
    }

    public void testPriorityOrder() {
        System.out.println("测试优先级顺序...");
        
        // 1. 表达式格式 ${} 优先级最高
        String template1 = "Hello ${name} and {name}";
        Map<String, Object> args = new HashMap<>();
        args.put("name", "John");
        String result1 = interpolator.interpolate(template1, args, Locale.ENGLISH);
        String expected1 = "Hello John and {name}"; // {name} 不会被处理
        if (expected1.equals(result1)) {
            System.out.println("✓ 通过: 表达式格式优先级最高");
        } else {
            System.out.println("✗ 失败: 表达式格式优先级测试失败");
        }
        
        // 2. MessageFormat 格式 {0} 优先级次之
        String template2 = "Hello {0} and {name}";
        Object[] args2 = {"John"};
        String result2 = interpolator.interpolate(template2, args2, Locale.ENGLISH);
        String expected2 = "Hello John and {name}"; // {name} 不会被处理
        if (expected2.equals(result2)) {
            System.out.println("✓ 通过: MessageFormat格式优先级次之");
        } else {
            System.out.println("✗ 失败: MessageFormat格式优先级测试失败");
        }
        
        // 3. 命名参数格式 {name} 优先级再次之
        String template3 = "Hello {name} and {}";
        String result3 = interpolator.interpolate(template3, args, Locale.ENGLISH);
        String expected3 = "Hello John and {}"; // {} 不会被处理
        if (expected3.equals(result3)) {
            System.out.println("✓ 通过: 命名参数格式优先级再次之");
        } else {
            System.out.println("✗ 失败: 命名参数格式优先级测试失败");
        }
        
        // 4. 简单占位符格式 {} 优先级最低
        String template4 = "Hello {} and {0}";
        Object[] args4 = {"John"};
        String result4 = interpolator.interpolate(template4, args4, Locale.ENGLISH);
        String expected4 = "Hello John and {0}"; // {0} 不会被处理
        if (expected4.equals(result4)) {
            System.out.println("✓ 通过: 简单占位符格式优先级最低");
        } else {
            System.out.println("✗ 失败: 简单占位符格式优先级测试失败");
        }
    }
} 