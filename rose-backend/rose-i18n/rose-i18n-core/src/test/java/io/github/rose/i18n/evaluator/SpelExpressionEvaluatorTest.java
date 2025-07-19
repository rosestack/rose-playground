package io.github.rose.i18n.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpelExpressionEvaluator 测试类
 * <p>
 * 测试 Spring Expression Language (SpEL) 表达式评估器的各种功能
 */
public class SpelExpressionEvaluatorTest {

    private SpelExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SpelExpressionEvaluator();
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
        assertEquals(2, result);
    }

    // ==================== 简单表达式测试 ====================

    @Test
    void testSimpleArithmetic() {
        Object result = evaluator.evaluate("2 + 3", new HashMap<>(), Locale.ENGLISH);
        assertEquals(5, result);
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

        Object result = evaluator.evaluate("#name", variables, Locale.ENGLISH);
        assertEquals("John", result);
    }

    @Test
    void testVariableInExpression() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 10);
        variables.put("y", 5);

        Object result = evaluator.evaluate("#x + #y", variables, Locale.ENGLISH);
        assertEquals(15, result);
    }

    @Test
    void testMissingVariable_ShouldReturnNull() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");

        Object result = evaluator.evaluate("#age", variables, Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testNullVariable() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", null);

        Object result = evaluator.evaluate("#name", variables, Locale.ENGLISH);
        assertNull(result);
    }

    // ==================== 对象属性访问测试 ====================

    @Test
    void testObjectPropertyAccess() {
        Map<String, Object> variables = new HashMap<>();
        TestUser user = new TestUser("Alice", 30);
        variables.put("user", user);

        Object result = evaluator.evaluate("#user.name", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
    }

    @Test
    void testNestedObjectPropertyAccess() {
        Map<String, Object> variables = new HashMap<>();
        TestAddress address = new TestAddress("123 Main St", "New York");
        TestUser user = new TestUser("Bob", 35);
        user.setAddress(address);
        variables.put("user", user);

        Object result = evaluator.evaluate("#user.address.city", variables, Locale.ENGLISH);
        assertEquals("New York", result);
    }

    @Test
    void testMapPropertyAccess() {
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", "Charlie");
        userMap.put("age", 28);
        variables.put("user", userMap);

        Object result = evaluator.evaluate("#user['name']", variables, Locale.ENGLISH);
        assertEquals("Charlie", result);
    }

    // ==================== 集合操作测试 ====================

    @Test
    void testListAccess() {
        Map<String, Object> variables = new HashMap<>();
        java.util.List<String> names = java.util.Arrays.asList("Alice", "Bob", "Charlie");
        variables.put("names", names);

        Object result = evaluator.evaluate("#names[0]", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
    }

    @Test
    void testArrayAccess() {
        Map<String, Object> variables = new HashMap<>();
        int[] numbers = {1, 2, 3, 4, 5};
        variables.put("numbers", numbers);

        Object result = evaluator.evaluate("#numbers[2]", variables, Locale.ENGLISH);
        assertEquals(3, result);
    }

    @Test
    void testListSize() {
        Map<String, Object> variables = new HashMap<>();
        java.util.List<String> names = java.util.Arrays.asList("Alice", "Bob", "Charlie");
        variables.put("names", names);

        Object result = evaluator.evaluate("#names.size()", variables, Locale.ENGLISH);
        assertEquals(3, result);
    }

    // ==================== 条件表达式测试 ====================

    @Test
    void testTernaryOperator() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("age", 18);

        Object result = evaluator.evaluate("#age >= 18 ? 'Adult' : 'Minor'", variables, Locale.ENGLISH);
        assertEquals("Adult", result);
    }

    @Test
    void testLogicalOperators() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("isStudent", true);
        variables.put("hasPermission", false);

        Object result = evaluator.evaluate("#isStudent and #hasPermission", variables, Locale.ENGLISH);
        assertEquals(false, result);
    }

    // ==================== 国际化测试 ====================

    @Test
    void testLocaleInjection() {
        Map<String, Object> variables = new HashMap<>();
        Locale locale = Locale.CHINESE;

        Object result = evaluator.evaluate("#locale", variables, locale);
        assertEquals(locale, result);
    }

    @Test
    void testLocaleLanguage() {
        Map<String, Object> variables = new HashMap<>();
        Locale locale = Locale.FRENCH;

        Object result = evaluator.evaluate("#language", variables, locale);
        assertEquals("fr", result);
    }

    @Test
    void testLocaleCountry() {
        Map<String, Object> variables = new HashMap<>();
        Locale locale = Locale.US;

        Object result = evaluator.evaluate("#country", variables, locale);
        assertEquals("US", result);
    }

    @Test
    void testDisplayLanguage() {
        Map<String, Object> variables = new HashMap<>();
        Locale locale = Locale.GERMAN;

        Object result = evaluator.evaluate("#displayLanguage", variables, locale);
        assertEquals("Deutsch", result);
    }

    // ==================== 工具类注入测试 ====================

    @Test
    void testMathClassInjection() {
        Object result = evaluator.evaluate("T(java.lang.Math).PI", new HashMap<>(), Locale.ENGLISH);
        assertEquals(Math.PI, result);
    }

    @Test
    void testArraysClassInjection() {
        Map<String, Object> variables = new HashMap<>();
        int[] numbers = {1, 2, 3};
        variables.put("numbers", numbers);

        Object result = evaluator.evaluate("T(java.util.Arrays).toString(#numbers)", variables, Locale.ENGLISH);
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    void testCollectionsClassInjection() {
        Map<String, Object> variables = new HashMap<>();
        java.util.List<String> list = java.util.Arrays.asList("a", "b", "c");
        variables.put("list", list);

        Object result = evaluator.evaluate("T(java.util.Collections).sort(#list)", variables, Locale.ENGLISH);
        assertNull(result); // sort 方法返回 void
    }

    // ==================== 表达式验证测试 ====================

    @Test
    void testIsValidExpression_Valid() {
        // 测试有效的表达式能够正常评估
        Object result1 = evaluator.evaluate("1 + 1", new HashMap<>(), Locale.ENGLISH);
        assertNotNull(result1);

        Object result2 = evaluator.evaluate("#name", new HashMap<>(), Locale.ENGLISH);
        // 即使变量不存在，表达式语法也是有效的
        assertNull(result2);

        Object result3 = evaluator.evaluate("'Hello World'", new HashMap<>(), Locale.ENGLISH);
        assertEquals("Hello World", result3);
    }

    @Test
    void testIsValidExpression_Invalid() {
        // 测试无效的表达式返回 null
        Object result1 = evaluator.evaluate("invalid.expression[", new HashMap<>(), Locale.ENGLISH);
        assertNull(result1);

        Object result2 = evaluator.evaluate("1 + ", new HashMap<>(), Locale.ENGLISH);
        assertNull(result2);
    }

    // ==================== 表达式类型测试 ====================

    @Test
    void testExpressionType_Number() {
        Object result = evaluator.evaluate("1 + 1", new HashMap<>(), Locale.ENGLISH);
        assertTrue(result instanceof Integer);
        assertEquals(2, result);
    }

    @Test
    void testExpressionType_String() {
        Object result = evaluator.evaluate("'Hello'", new HashMap<>(), Locale.ENGLISH);
        assertTrue(result instanceof String);
        assertEquals("Hello", result);
    }

    @Test
    void testExpressionType_Boolean() {
        Object result = evaluator.evaluate("true", new HashMap<>(), Locale.ENGLISH);
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    void testExpressionType_Invalid() {
        Object result = evaluator.evaluate("invalid.expression[", new HashMap<>(), Locale.ENGLISH);
        assertNull(result);
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
        assertNull(result);
    }

    @Test
    void testNullPointerAccess_ShouldReturnNull() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", null);

        Object result = evaluator.evaluate("#user.name", variables, Locale.ENGLISH);
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
            evaluator.evaluate("#x + #y", variables, Locale.ENGLISH);
        }

        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 5000, "Performance test should complete within 5 seconds");
    }

    // ==================== 复杂表达式测试 ====================

    @Test
    void testComplexExpression() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", new TestUser("Alice", 25));
        variables.put("isVIP", true);

        Object result = evaluator.evaluate(
                "#isVIP ? 'Welcome VIP ' + #user.name : 'Welcome ' + #user.name",
                variables,
                Locale.ENGLISH
        );
        assertEquals("Welcome VIP Alice", result);
    }

    @Test
    void testMethodCall() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "john");

        Object result = evaluator.evaluate("#name.toUpperCase()", variables, Locale.ENGLISH);
        assertEquals("JOHN", result);
    }

    @Test
    void testCollectionOperations() {
        Map<String, Object> variables = new HashMap<>();
        java.util.List<Integer> numbers = java.util.Arrays.asList(1, 2, 3, 4, 5);
        variables.put("numbers", numbers);

        Object result = evaluator.evaluate("#numbers.?[#this > 3]", variables, Locale.ENGLISH);
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
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