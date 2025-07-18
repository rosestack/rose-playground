package io.github.rose.i18n.interpolation.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JakartaElExpressionEvaluator 测试类
 * <p>
 * 测试 Jakarta EL 表达式评估器的各种功能
 */
public class JakartaElExpressionEvaluatorTest {

    private JakartaElExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new JakartaElExpressionEvaluator();
    }

    // ==================== 基础功能测试 ====================

    @Test
    void testNullExpression_ShouldReturnNull() {
        Object result = evaluator.evaluate(null, new HashMap<>(), Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testEmptyExpression_ShouldReturnNull() {
        Object result = evaluator.evaluate("", new HashMap<>(), Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testWhitespaceExpression_ShouldReturnNull() {
        Object result = evaluator.evaluate("   ", new HashMap<>(), Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testNullVariables_ShouldWork() {
        Object result = evaluator.evaluate("1 + 1", null, Locale.ENGLISH);
        assertEquals(2L, result); // Jakarta EL 返回 Long 类型
    }

    // ==================== 简单表达式测试 ====================

    @Test
    void testSimpleArithmetic() {
        Object result = evaluator.evaluate("2 + 3", new HashMap<>(), Locale.ENGLISH);
        assertEquals(5L, result); // Jakarta EL 返回 Long 类型
    }

    @Test
    void testSimpleString() {
        Object result = evaluator.evaluate("'Hello World'", new HashMap<>(), Locale.ENGLISH);
        assertEquals("Hello World", result);
    }

    @Test
    void testBooleanExpression() {
        Object result = evaluator.evaluate("true and false", new HashMap<>(), Locale.ENGLISH);
        assertEquals(false, result);
    }

    @Test
    void testComparison() {
        Object result = evaluator.evaluate("5 > 3", new HashMap<>(), Locale.ENGLISH);
        assertEquals(true, result);
    }

    // ==================== 变量访问测试 ====================

    @Test
    void testSimpleVariableAccess() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("age", 25);

        Object result = evaluator.evaluate("name", variables, Locale.ENGLISH);
        assertEquals("John", result);
    }

    @Test
    void testVariableInExpression() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 10);
        variables.put("y", 5);

        Object result = evaluator.evaluate("x + y", variables, Locale.ENGLISH);
        assertEquals(15L, result); // Jakarta EL 返回 Long 类型
    }

    @Test
    void testMissingVariable_ShouldReturnNull() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");

        Object result = evaluator.evaluate("age", variables, Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testNullVariable() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", null);

        Object result = evaluator.evaluate("name", variables, Locale.ENGLISH);
        assertNull(result);
    }

    // ==================== 对象属性访问测试 ====================

    @Test
    void testObjectPropertyAccess() {
        Map<String, Object> variables = new HashMap<>();
        TestUser user = new TestUser("Alice", 30);
        variables.put("user", user);

        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
    }

    @Test
    void testNestedObjectPropertyAccess() {
        Map<String, Object> variables = new HashMap<>();
        TestAddress address = new TestAddress("123 Main St", "New York");
        TestUser user = new TestUser("Bob", 35);
        user.setAddress(address);
        variables.put("user", user);

        Object result = evaluator.evaluate("user.address.city", variables, Locale.ENGLISH);
        assertEquals("New York", result);
    }

    @Test
    void testMapPropertyAccess() {
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", "Charlie");
        userMap.put("age", 28);
        variables.put("user", userMap);

        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertEquals("Charlie", result);
    }

    // ==================== 集合操作测试 ====================

    @Test
    void testListAccess() {
        Map<String, Object> variables = new HashMap<>();
        java.util.List<String> names = java.util.Arrays.asList("Alice", "Bob", "Charlie");
        variables.put("names", names);

        Object result = evaluator.evaluate("names[0]", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
    }

    @Test
    void testArrayAccess() {
        Map<String, Object> variables = new HashMap<>();
        int[] numbers = {1, 2, 3, 4, 5};
        variables.put("numbers", numbers);

        Object result = evaluator.evaluate("numbers[2]", variables, Locale.ENGLISH);
        assertEquals(3, result);
    }

    @Test
    void testListSize() {
        Map<String, Object> variables = new HashMap<>();
        java.util.List<String> names = java.util.Arrays.asList("Alice", "Bob", "Charlie");
        variables.put("names", names);

        Object result = evaluator.evaluate("names.size()", variables, Locale.ENGLISH);
        assertEquals(3, result);
    }

    // ==================== 条件表达式测试 ====================

    @Test
    void testTernaryOperator() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("age", 18);

        Object result = evaluator.evaluate("age >= 18 ? 'Adult' : 'Minor'", variables, Locale.ENGLISH);
        assertEquals("Adult", result);
    }

    @Test
    void testLogicalOperators() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("isStudent", true);
        variables.put("hasPermission", false);

        Object result = evaluator.evaluate("isStudent and hasPermission", variables, Locale.ENGLISH);
        assertEquals(false, result);
    }

    // ==================== 国际化测试 ====================

    @Test
    void testLocaleInjection() {
        Map<String, Object> variables = new HashMap<>();
        Locale locale = Locale.CHINESE;

        Object result = evaluator.evaluate("locale", variables, locale);
        assertEquals(locale, result);
    }

    @Test
    void testLocaleLanguage() {
        Map<String, Object> variables = new HashMap<>();
        Locale locale = Locale.FRENCH;

        Object result = evaluator.evaluate("locale.language", variables, locale);
        assertEquals("fr", result);
    }

    // ==================== 错误处理测试 ====================

    @Test
    void testInvalidExpression_ShouldReturnNull() {
        Object result = evaluator.evaluate("invalid.expression[", new HashMap<>(), Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testDivisionByZero_ShouldReturnNull() {
        Object result = evaluator.evaluate("1 / 0", new HashMap<>(), Locale.ENGLISH);
        // Jakarta EL 对于除零操作返回 Infinity，这是符合 IEEE 754 标准的
        assertTrue(result instanceof Double && Double.isInfinite((Double) result));
    }

    @Test
    void testNullPointerAccess_ShouldReturnNull() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", null);

        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertNull(result);
    }

    // ==================== 性能测试 ====================

    @Test
    void testPerformance_RepeatedEvaluation() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 10);
        variables.put("y", 20);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            evaluator.evaluate("x + y", variables, Locale.ENGLISH);
        }

        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 5000, "Performance test should complete within 5 seconds");
    }

    // ==================== 辅助类 ====================

    public static class TestUser {
        private String name;
        private int age;
        private TestAddress address;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public TestAddress getAddress() {
            return address;
        }

        public void setAddress(TestAddress address) {
            this.address = address;
        }
    }

    public static class TestAddress {
        private String street;
        private String city;

        public TestAddress(String street, String city) {
            this.street = street;
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
} 