package io.github.rose.i18n.interpolation;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表达式评估器测试类
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
class ExpressionEvaluatorTest {

    @Test
    void testSimpleExpressionEvaluator() {
        SimpleExpressionEvaluator evaluator = new SimpleExpressionEvaluator();

        // 测试简单属性访问
        TestUser user = new TestUser("John", 25);
        Map<String, Object> variables = Map.of("user", user);

        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertEquals("John", result);

        result = evaluator.evaluate("user.age", variables, Locale.ENGLISH);
        assertEquals(25, result);

        // 测试方法调用
        result = evaluator.evaluate("user.getName()", variables, Locale.ENGLISH);
        assertEquals("John", result);

        // 测试空值检查
        result = evaluator.evaluate("user.name != null", variables, Locale.ENGLISH);
        assertEquals(true, result);

        // 测试无效表达式
        result = evaluator.evaluate("user.nonExistentProperty", variables, Locale.ENGLISH);
        assertNull(result);

        // 测试null和空表达式
        result = evaluator.evaluate(null, variables, Locale.ENGLISH);
        assertNull(result);

        result = evaluator.evaluate("", variables, Locale.ENGLISH);
        assertNull(result);

        result = evaluator.evaluate("   ", variables, Locale.ENGLISH);
        assertNull(result);
    }
    
    @Test
    void testDefaultMessageInterpolatorWithExpressions() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试简单表达式
        TestUser user = new TestUser("Alice", 30);
        Map<String, Object> params = Map.of("user", user);

        String template = "Hello ${user.name}!";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Hello Alice!", result);

        // 测试方法调用
        template = "Name: ${user.getName()}, Age: ${user.getAge()}";
        result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Name: Alice, Age: 30", result);

        // 测试数组参数
        template = "Hello ${arg0}!";
        result = interpolator.interpolate(template, new Object[]{"World"}, Locale.ENGLISH);
        assertEquals("Hello World!", result);
    }

    @Test
    void testDefaultMessageInterpolatorWithMessageFormat() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试MessageFormat风格
        String template = "Hello {0}, you are {1} years old!";
        String result = interpolator.interpolate(template, new Object[]{"Alice", 25}, Locale.ENGLISH);
        assertEquals("Hello Alice, you are 25 years old!", result);

        // 测试空参数
        result = interpolator.interpolate(template, new Object[]{}, Locale.ENGLISH);
        assertEquals(template, result); // 应该返回原模板

        // 测试null参数
        result = interpolator.interpolate(template, (Object) null, Locale.ENGLISH);
        assertEquals(template, result);
    }

    @Test
    void testDefaultMessageInterpolatorWithNamedParameters() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试命名参数风格
        String template = "Hello {name}, you are {age} years old!";
        Map<String, Object> params = Map.of("name", "Bob", "age", 30);
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Hello Bob, you are 30 years old!", result);

        // 测试缺少参数
        params = Map.of("name", "Bob");
        result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Hello Bob, you are {age} years old!", result);
    }

    @Test
    void testDefaultMessageInterpolatorWithCustomEvaluator() {
        // 测试使用自定义表达式评估器
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();
        interpolator.setExpressionEvaluator(new SimpleExpressionEvaluator());

        TestUser user = new TestUser("Bob", 20);
        Map<String, Object> params = Map.of("user", user);

        String template = "User: ${user.name}, Age: ${user.age}";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("User: Bob, Age: 20", result);
    }

    @Test
    void testDefaultMessageInterpolatorEdgeCases() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试null模板
        String result = interpolator.interpolate(null, Map.of("key", "value"), Locale.ENGLISH);
        assertNull(result);

        // 测试空模板
        result = interpolator.interpolate("", Map.of("key", "value"), Locale.ENGLISH);
        assertEquals("", result);

        // 测试无占位符的模板
        result = interpolator.interpolate("Hello World", Map.of("key", "value"), Locale.ENGLISH);
        assertEquals("Hello World", result);

        // 测试混合占位符
        String template = "Hello {name}, you have ${count} messages and {0} notifications";
        Map<String, Object> params = Map.of("name", "Alice", "count", 5);
        result = interpolator.interpolate(template, params, Locale.ENGLISH);
        // 应该优先处理表达式
        assertTrue(result.contains("5 messages"));
    }

    // 测试用的辅助类
    public static class TestUser {
        private String name;
        private int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
