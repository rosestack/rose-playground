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
        ExpressionEvaluator evaluator = new SimpleExpressionEvaluator();

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
