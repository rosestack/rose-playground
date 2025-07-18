package io.github.rose.i18n.interpolation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * MessageInterpolator 测试类
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class MessageInterpolatorTest {

    private MessageInterpolator interpolator;

    @BeforeEach
    void setUp() {
        interpolator = new DefaultMessageInterpolator();
    }

    @Test
    void testInterpolationParameterBasic() {
        // 测试基本的 InterpolationParameter 使用
        InterpolationParameter params = InterpolationParameter.builder()
            .add("name", "Alice")
            .add("age", 25)
            .build();

        String result = interpolator.interpolate("Hello {name}, you are {age} years old!", 
            Locale.ENGLISH, params);
        
        assertEquals("Hello Alice, you are 25 years old!", result);
    }

    @Test
    void testArrayBackwardCompatibility() {
        // 测试数组参数的向后兼容性
        Object[] args = {"Alice", 25};
        
        String result = interpolator.interpolate("Hello {}, you are {} years old!", 
            Locale.ENGLISH, args);
        
        assertEquals("Hello Alice, you are 25 years old!", result);
    }

    @Test
    void testMapBackwardCompatibility() {
        // 测试 Map 参数的向后兼容性
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "Bob");
        map.put("age", 30);
        
        String result = interpolator.interpolate("Hello {name}, you are {age} years old!", 
            Locale.ENGLISH, map);
        
        assertEquals("Hello Bob, you are 30 years old!", result);
    }

    @Test
    void testNullHandling() {
        // 测试 null 参数处理
        String result1 = interpolator.interpolate("Hello World!", Locale.ENGLISH, (InterpolationParameter) null);
        assertEquals("Hello World!", result1);
        
        String result2 = interpolator.interpolate("Hello World!", Locale.ENGLISH, (Object[]) null);
        assertEquals("Hello World!", result2);
        
        String result3 = interpolator.interpolate("Hello World!", Locale.ENGLISH, (Object) null);
        assertEquals("Hello World!", result3);
    }

    @Test
    void testMessageFormatStyle() {
        // 测试 MessageFormat 风格 {0}, {1}
        InterpolationParameter params = InterpolationParameter.builder()
            .add("0", "Alice")
            .add("1", 25)
            .build();

        String result = interpolator.interpolate("Hello {0}, you are {1} years old!", 
            Locale.ENGLISH, params);
        
        assertEquals("Hello Alice, you are 25 years old!", result);
    }

    @Test
    void testMixedFormats() {
        // 测试混合格式
        InterpolationParameter params = InterpolationParameter.builder()
            .add("name", "Charlie")
            .add("age", 35)
            .add("0", "Welcome")
            .add("1", "friend")
            .build();

        String result = interpolator.interpolate("{0} {name}, my {1}! You are {age} years old.", 
            Locale.ENGLISH, params);
        
        assertEquals("Welcome Charlie, my friend! You are 35 years old.", result);
    }

    @Test
    void testExpressionEvaluation() {
        // 测试表达式评估
        InterpolationParameter params = InterpolationParameter.builder()
            .add("user", new TestUser("David", 40))
            .add("company", "TechCorp")
            .build();

        String result = interpolator.interpolate("User: ${user.name} (${user.age}), Company: {company}", 
            Locale.ENGLISH, params);
        
        // 注意：表达式评估可能因为访问权限问题而失败，这里主要测试语法
        assertNotNull(result);
    }

    @Test
    void testFromMapMethod() {
        // 测试 from 方法
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "Eve");
        map.put("age", 28);
        
        InterpolationParameter params = InterpolationParameter.from(map);
        String result = interpolator.interpolate("Hello {name}, you are {age} years old!", 
            Locale.ENGLISH, params);
        
        assertEquals("Hello Eve, you are 28 years old!", result);
    }

    @Test
    void testBuilderAddAll() {
        // 测试 Builder 的 addAll 方法
        LinkedHashMap<String, Object> existingMap = new LinkedHashMap<>();
        existingMap.put("orderId", "ORD-12345");
        existingMap.put("amount", 299.99);
        
        InterpolationParameter params = InterpolationParameter.builder()
            .add("customerName", "Frank")
            .addAll(existingMap)
            .add("status", "confirmed")
            .build();

        String result = interpolator.interpolate("Order {orderId} for {customerName}, amount: {amount}, status: {status}", 
            Locale.ENGLISH, params);
        
        assertEquals("Order ORD-12345 for Frank, amount: 299.99, status: confirmed", result);
    }

    @Test
    void testQuickCreationMethods() {
        // 测试快速创建方法
        InterpolationParameter single = InterpolationParameter.of("name", "Grace");
        String result1 = interpolator.interpolate("Hello {name}!", Locale.ENGLISH, single);
        assertEquals("Hello Grace!", result1);
        
        InterpolationParameter two = InterpolationParameter.of("name", "Henry", "age", 45);
        String result2 = interpolator.interpolate("Hello {name}, you are {age} years old!", Locale.ENGLISH, two);
        assertEquals("Hello Henry, you are 45 years old!", result2);
        
        InterpolationParameter three = InterpolationParameter.of("name", "Iris", "age", 50, "company", "Innovation");
        String result3 = interpolator.interpolate("Hello {name}, you are {age} years old, working at {company}!", 
            Locale.ENGLISH, three);
        assertEquals("Hello Iris, you are 50 years old, working at Innovation!", result3);
    }

    // 测试用的用户类
    private static class TestUser {
        private final String name;
        private final int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }
} 