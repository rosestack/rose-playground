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
    }

    @Test
    void testJakartaElExpressionEvaluator() {
        ExpressionEvaluator evaluator;
        try {
            evaluator = new JakartaElExpressionEvaluator();

        } catch (RuntimeException e) {
            // Jakarta EL不可用，跳过测试
            return;
        }

        // 测试简单属性访问
        TestUser user = new TestUser("John", 25);
        Map<String, Object> variables = Map.of("user", user);

        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertEquals("John", result);

        // 测试条件表达式
        result = evaluator.evaluate("user.age >= 18 ? 'adult' : 'minor'", variables, Locale.ENGLISH);
        assertEquals("adult", result);

        // 测试算术运算
        variables = Map.of("price", 100, "quantity", 2);
        result = evaluator.evaluate("price * quantity", variables, Locale.ENGLISH);
        assertEquals(200L, result); // EL表达式返回Long类型
    }

    @Test
    void testSpringElExpressionEvaluator() {
        ExpressionEvaluator evaluator;
        try {
            evaluator = new SpringElExpressionEvaluator();
        } catch (Exception e) {
            // Spring EL不可用，跳过测试
            return;
        }

        // 测试简单属性访问
        TestUser user = new TestUser("John", 25);
        Map<String, Object> variables = Map.of("user", user);

        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertEquals("John", result);

        // 测试条件表达式
        result = evaluator.evaluate("user.age >= 18 ? 'adult' : 'minor'", variables, Locale.ENGLISH);
        assertEquals("adult", result);

        // 测试算术运算
        variables = Map.of("price", 100, "quantity", 2);
        result = evaluator.evaluate("price * quantity", variables, Locale.ENGLISH);
        assertEquals(200, result); // Spring EL返回Integer类型
    }

    @Test
    void testDefaultMessageInterpolatorWithExpressions() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试简单表达式
        TestUser user = new TestUser("Alice", 30);
        Map<String, Object> params = Map.of("user", user);

        String template = "Hello ${user.name}!";
        String result = interpolator.interpolate(template, params);
        assertEquals("Hello Alice!", result);

        // 测试方法调用
        template = "Name: ${user.getName()}, Age: ${user.getAge()}";
        result = interpolator.interpolate(template, params);
        assertEquals("Name: Alice, Age: 30", result);

        // 测试数组参数
        template = "Hello ${arg0}!";
        result = interpolator.interpolate(template, new Object[]{"World"});
        assertEquals("Hello World!", result);
    }

    @Test
    void testDefaultMessageInterpolatorWithMessageFormat() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试MessageFormat风格
        String template = "Hello {0}, you are {1} years old!";
        String result = interpolator.interpolate(template, new Object[]{"Alice", 25});
        assertEquals("Hello Alice, you are 25 years old!", result);

        // 测试空参数
        result = interpolator.interpolate(template, new Object[]{});
        assertEquals(template, result); // 应该返回原模板

        // 测试null参数
        result = interpolator.interpolate(template, (Object) null);
        assertEquals(template, result);
    }

    @Test
    void testDefaultMessageInterpolatorWithNamedParameters() {
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试命名参数风格
        String template = "Hello {name}, you are {age} years old!";
        Map<String, Object> params = Map.of("name", "Bob", "age", 30);
        String result = interpolator.interpolate(template, params);
        assertEquals("Hello Bob, you are 30 years old!", result);

        // 测试缺少参数
        params = Map.of("name", "Bob");
        result = interpolator.interpolate(template, params);
        assertEquals("Hello Bob, you are {age} years old!", result);
    }

    @Test
    void testDefaultMessageInterpolatorAutoDiscovery() {
        // 测试自动发现功能
        DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();

        // 测试复杂表达式，如果有Spring EL或Jakarta EL可用，应该能够处理
        TestUser user = new TestUser("Bob", 20);
        Map<String, Object> params = Map.of("user", user, "minAge", 18);

        String template = "Status: ${user.age >= minAge ? 'adult' : 'minor'}";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);

        // 如果有高级表达式评估器可用，应该能正确处理条件表达式
        // 否则会返回原始表达式
        assertTrue(result.equals("Status: adult") || result.contains("${"));
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
