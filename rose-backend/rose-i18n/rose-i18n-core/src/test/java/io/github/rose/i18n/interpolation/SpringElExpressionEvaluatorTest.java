package io.github.rose.i18n.interpolation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfClassPresent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring EL表达式评估器测试
 */
@EnabledIfClassPresent("org.springframework.expression.ExpressionParser")
class SpringElExpressionEvaluatorTest {

    private final SpringElExpressionEvaluator evaluator = new SpringElExpressionEvaluator();

    @Test
    void testBasicProperties() {
        assertTrue(evaluator.isAvailable());
        assertEquals("SpringElExpressionEvaluator", evaluator.getName());
        assertEquals(5, evaluator.getPriority());
        assertTrue(evaluator.supports("any expression"));
    }

    @Test
    void testSimplePropertyAccess() {
        TestUser user = new TestUser("Alice", 30);
        Map<String, Object> variables = Map.of("user", user);
        
        Object result = evaluator.evaluate("user.name", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
        
        result = evaluator.evaluate("user.age", variables, Locale.ENGLISH);
        assertEquals(30, result);
    }

    @Test
    void testMethodCall() {
        TestUser user = new TestUser("Bob", 25);
        Map<String, Object> variables = Map.of("user", user);
        
        Object result = evaluator.evaluate("user.getName()", variables, Locale.ENGLISH);
        assertEquals("Bob", result);
        
        result = evaluator.evaluate("user.getAge()", variables, Locale.ENGLISH);
        assertEquals(25, result);
    }

    @Test
    void testConditionalExpression() {
        TestUser user1 = new TestUser("Adult", 25);
        TestUser user2 = new TestUser("Minor", 16);
        
        Map<String, Object> variables1 = Map.of("user", user1);
        Object result1 = evaluator.evaluate("user.age >= 18 ? 'adult' : 'minor'", variables1, Locale.ENGLISH);
        assertEquals("adult", result1);
        
        Map<String, Object> variables2 = Map.of("user", user2);
        Object result2 = evaluator.evaluate("user.age >= 18 ? 'adult' : 'minor'", variables2, Locale.ENGLISH);
        assertEquals("minor", result2);
    }

    @Test
    void testArithmeticOperations() {
        Map<String, Object> variables = Map.of(
            "price", 100,
            "quantity", 3,
            "tax", 0.1
        );
        
        Object result = evaluator.evaluate("price * quantity", variables, Locale.ENGLISH);
        assertEquals(300, result);
        
        result = evaluator.evaluate("price * quantity * (1 + tax)", variables, Locale.ENGLISH);
        assertEquals(330.0, result);
    }

    @Test
    void testLogicalOperations() {
        TestUser user = new TestUser("John", 25);
        user.setActive(true);
        user.setVerified(true);
        
        Map<String, Object> variables = Map.of("user", user);
        
        Object result = evaluator.evaluate("user.active && user.verified", variables, Locale.ENGLISH);
        assertEquals(true, result);
        
        user.setVerified(false);
        result = evaluator.evaluate("user.active && user.verified", variables, Locale.ENGLISH);
        assertEquals(false, result);
    }

    @Test
    void testCollectionOperations() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Map<String, String> userMap = Map.of("admin", "Alice", "user", "Bob");
        
        Map<String, Object> variables = Map.of(
            "names", names,
            "userMap", userMap
        );
        
        Object result = evaluator.evaluate("names[0]", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
        
        result = evaluator.evaluate("names.size()", variables, Locale.ENGLISH);
        assertEquals(3, result);
        
        result = evaluator.evaluate("userMap['admin']", variables, Locale.ENGLISH);
        assertEquals("Alice", result);
    }

    @Test
    void testNullSafeOperations() {
        Map<String, Object> variables = Map.of("user", new TestUser(null, 25));
        
        Object result = evaluator.evaluate("user.name != null ? user.name : 'Anonymous'", variables, Locale.ENGLISH);
        assertEquals("Anonymous", result);
        
        result = evaluator.evaluate("user.name?.toUpperCase()", variables, Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testStringOperations() {
        Map<String, Object> variables = Map.of("name", "alice");
        
        Object result = evaluator.evaluate("name.toUpperCase()", variables, Locale.ENGLISH);
        assertEquals("ALICE", result);
        
        result = evaluator.evaluate("name.substring(0, 3)", variables, Locale.ENGLISH);
        assertEquals("ali", result);
        
        result = evaluator.evaluate("name.length()", variables, Locale.ENGLISH);
        assertEquals(5, result);
    }

    @Test
    void testRegularExpressions() {
        Map<String, Object> variables = Map.of("email", "user@example.com");
        
        Object result = evaluator.evaluate("email matches '.*@.*\\.com'", variables, Locale.ENGLISH);
        assertEquals(true, result);
        
        variables = Map.of("email", "invalid-email");
        result = evaluator.evaluate("email matches '.*@.*\\.com'", variables, Locale.ENGLISH);
        assertEquals(false, result);
    }

    @Test
    void testTypeOperations() {
        Map<String, Object> variables = new HashMap<>();
        
        Object result = evaluator.evaluate("T(Math).PI", variables, Locale.ENGLISH);
        assertEquals(Math.PI, result);
        
        result = evaluator.evaluate("T(String).valueOf(123)", variables, Locale.ENGLISH);
        assertEquals("123", result);
    }

    @Test
    void testInvalidExpression() {
        Map<String, Object> variables = Map.of("user", new TestUser("Alice", 25));
        
        // 无效表达式应该返回null
        Object result = evaluator.evaluate("user.nonExistentProperty", variables, Locale.ENGLISH);
        assertNull(result);
        
        result = evaluator.evaluate("invalid syntax !!!", variables, Locale.ENGLISH);
        assertNull(result);
    }

    @Test
    void testEmptyOrNullExpression() {
        Map<String, Object> variables = Map.of("user", new TestUser("Alice", 25));
        
        Object result = evaluator.evaluate(null, variables, Locale.ENGLISH);
        assertNull(result);
        
        result = evaluator.evaluate("", variables, Locale.ENGLISH);
        assertNull(result);
        
        result = evaluator.evaluate("   ", variables, Locale.ENGLISH);
        assertNull(result);
    }

    /**
     * 测试用户类
     */
    public static class TestUser {
        private String name;
        private int age;
        private boolean active;
        private boolean verified;

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

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }
    }
}
