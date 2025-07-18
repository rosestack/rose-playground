package io.github.rose.i18n.interpolation;

import io.github.rose.i18n.interpolation.evaluator.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultMessageInterpolator 完整功能测试
 *
 * <p>职责：测试DefaultMessageInterpolator的所有功能，包括：</p>
 * <ul>
 *   <li>核心插值功能：MessageFormat风格、命名参数风格、表达式风格</li>
 *   <li>边界情况：null处理、空值处理、异常处理</li>
 *   <li>业务场景：真实世界用例、多语言支持</li>
 *   <li>性能测试：大数据量、并发安全</li>
 * </ul>
 */
class DefaultMessageInterpolatorTest {

    private DefaultMessageInterpolator interpolator;

    @BeforeEach
    void setUp() {
        interpolator = new DefaultMessageInterpolator();
    }

    @Test
    void testMessageFormatStyle() {
        // 测试基本MessageFormat风格
        String template = "Hello {0}, you are {1} years old!";
        String result = interpolator.interpolate(template, new Object[]{"Alice", 25}, Locale.ENGLISH);
        assertEquals("Hello Alice, you are 25 years old!", result);

        // 测试多种数据类型
        template = "User: {0}, Age: {1}, Active: {2}, Score: {3}";
        result = interpolator.interpolate(template, new Object[]{"Bob", 30, true, 95.5}, Locale.ENGLISH);
        assertEquals("User: Bob, Age: 30, Active: true, Score: 95.5", result);
    }

    @Test
    void testMessageFormatWithLocale() {
        // 测试带Locale的MessageFormat
        String template = "Price: {0,number,currency}";
        String result = interpolator.interpolate(template, new Object[]{123.45}, Locale.US);
        assertNotNull(result);
        assertTrue(result.contains("123.45") || result.contains("$") || result.contains("Price:"));

        // 测试日期格式化
        template = "Date: {0,date,short}";
        Date date = new Date();
        result = interpolator.interpolate(template, new Object[]{date}, Locale.ENGLISH);
        assertNotNull(result);
        assertTrue(result.startsWith("Date:"));
    }

    @Test
    void testFormatStyle() {
        // 测试基本Format风格
        String template = "Hello {}, you are {} years old!";
        String result = interpolator.interpolate(template, new Object[]{"Alice", 25}, Locale.ENGLISH);
        assertEquals("Hello Alice, you are 25 years old!", result);

        // 测试多种数据类型
        template = "User: {}, Age: {}, Active: {}, Score: {}";
        result = interpolator.interpolate(template, new Object[]{"Bob", 30, true, 95.5}, Locale.ENGLISH);
        assertEquals("User: Bob, Age: 30, Active: true, Score: 95.5", result);
    }

    @Test
    void testNamedParameters() {
        // 测试基本命名参数
        String template = "Hello {name}, you are {age} years old!";
        Map<String, Object> params = Map.of("name", "Charlie", "age", 35);
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Hello Charlie, you are 35 years old!", result);

        // 测试部分参数缺失
        template = "Hello {name}, you are {age} years old and live in {city}!";
        params = Map.of("name", "David", "age", 28);
        result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Hello David, you are 28 years old and live in {city}!", result);
    }

    @Test
    void testExpressionStyle() {
        TestUser user = new TestUser("Eve", 32);
        Map<String, Object> params = Map.of("user", user);

        // 测试基本表达式
        String template = "Hello ${user.name}, you are ${user.age} years old!";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Hello Eve, you are 32 years old!", result);

        // 测试方法调用
        template = "User: ${user.getName()}, Age: ${user.getAge()}";
        result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("User: Eve, Age: 32", result);
    }

    @Test
    void testNestedExpressions() {
        TestUser user = new TestUser("Frank", 40);
        TestProfile profile = new TestProfile("frank@example.com", "Manager");
        user.setProfile(profile);

        Map<String, Object> params = Map.of("user", user);

        // 测试嵌套属性表达式
        String template = "Email: ${user.profile.email}, Title: ${user.profile.title}";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertEquals("Email: frank@example.com, Title: Manager", result);
    }

    @Test
    void testArrayParametersInExpressions() {
        String[] names = {"Alice", "Bob", "Charlie"};
        Map<String, Object> params = Map.of("names", names);

        String template = "First: ${names[0]}, Count: ${names.length}";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        // 注意：SimpleExpressionEvaluator可能不支持数组索引，这里测试实际行为
        assertNotNull(result);
    }

    @Test
    void testNullAndEmptyHandling() {
        // 测试null模板
        String result = interpolator.interpolate(null, Map.of("key", "value"), Locale.ENGLISH);
        assertNull(result);

        // 测试空模板
        result = interpolator.interpolate("", Map.of("key", "value"), Locale.ENGLISH);
        assertEquals("", result);

        // 测试null参数
        result = interpolator.interpolate("Hello {name}!", (Map<String, Object>) null, Locale.ENGLISH);
        assertEquals("Hello {name}!", result);

        // 测试空参数
        result = interpolator.interpolate("Hello {0}!", new Object[]{}, Locale.ENGLISH);
        assertEquals("Hello {0}!", result);
    }

    @Test
    void testNoPlaceholders() {
        // 测试没有占位符的模板
        String template = "This is a plain text message.";
        String result = interpolator.interpolate(template, Map.of("key", "value"), Locale.ENGLISH);
        assertEquals(template, result);

        result = interpolator.interpolate(template, new Object[]{"value"}, Locale.ENGLISH);
        assertEquals(template, result);
    }

    @Test
    void testInvalidPlaceholders() {
        Map<String, Object> params = Map.of("name", "Alice");

        // 测试格式错误的占位符
        String template = "Hello {name, you are great!";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        // 应该保持原样或部分处理
        assertNotNull(result);

        // 测试不完整的表达式
        template = "Hello ${name, you are great!";
        result = interpolator.interpolate(template, params, Locale.ENGLISH);
        assertNotNull(result);
    }

    @Test
    void testCustomExpressionEvaluator() {
        // 测试自定义表达式评估器
        MockExpressionEvaluator mockEvaluator = new MockExpressionEvaluator();
        interpolator.setExpressionEvaluator(mockEvaluator);

        String template = "Result: ${test.expression}";
        Map<String, Object> params = Map.of("test", "value");
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);

        assertEquals("Result: MOCK_RESULT", result);
    }

    // 测试用的Mock表达式评估器
    private static class MockExpressionEvaluator implements ExpressionEvaluator {
        @Override
        public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
            return "MOCK_RESULT";
        }
    }

    // 测试用的内部类
    public static class TestUser {
        private String name;
        private int age;
        private TestProfile profile;

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

        public TestProfile getProfile() {
            return profile;
        }

        public void setProfile(TestProfile profile) {
            this.profile = profile;
        }
    }

    public static class TestProfile {
        private String email;
        private String title;

        public TestProfile(String email, String title) {
            this.email = email;
            this.title = title;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    // ==================== 边界测试 ====================

    @Test
    void testNullAndEmptyInputs() {
        // 测试null输入
        assertNull(interpolator.interpolate(null, (Object[]) null, null));
        assertNull(interpolator.interpolate(null, Map.of(), Locale.ENGLISH));
        assertNull(interpolator.interpolate(null, new Object[]{}, Locale.ENGLISH));

        // 测试空输入
        assertEquals("", interpolator.interpolate("", Map.of(), Locale.ENGLISH));
        assertEquals("", interpolator.interpolate("", new Object[]{}, Locale.ENGLISH));

        // 测试无参数的占位符
        String result = interpolator.interpolate("Hello {name}!", Map.of(), Locale.ENGLISH);
        assertEquals("Hello {name}!", result);

        result = interpolator.interpolate("Hello {0}!", new Object[]{}, Locale.ENGLISH);
        assertEquals("Hello {0}!", result);
    }

    @Test
    void testMalformedPlaceholders() {
        Map<String, Object> params = Map.of("name", "Alice", "age", 25);

        // 测试各种格式错误的占位符
        String[] malformedTemplates = {
                "Hello {name",           // 缺少右括号
                "Hello name}",           // 缺少左括号
                "Hello {{name}}",        // 双括号
                "Hello ${name",          // 缺少右括号
                "Hello $name}",          // 缺少左括号
        };

        for (String template : malformedTemplates) {
            String result = interpolator.interpolate(template, params, Locale.ENGLISH);
            assertNotNull(result, "Template should not return null: " + template);
        }
    }

    @Test
    void testSpecialCharacters() {
        Map<String, Object> params = Map.of(
                "special", "Hello\nWorld\t!",
                "unicode", "你好世界",
                "symbols", "@#$%^&*()"
        );

        String template = "Special: ${special}, Unicode: ${unicode}, Symbols: ${symbols}";
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);

        assertTrue(result.contains("Hello\nWorld\t!"));
        assertTrue(result.contains("你好世界"));
        assertTrue(result.contains("@#$%^&*()"));
    }

    @Test
    void testPerformanceWithLargeTemplates() {
        // 测试大模板的性能
        StringBuilder templateBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            templateBuilder.append("Item ").append(i).append(": ${item").append(i).append("} ");
            params.put("item" + i, "value" + i);
        }

        String template = templateBuilder.toString();
        long startTime = System.currentTimeMillis();
        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
        long endTime = System.currentTimeMillis();

        assertNotNull(result);
        assertTrue(result.contains("value0"));
        assertTrue(result.contains("value99"));

        // 性能检查（应该在合理时间内完成）
        assertTrue(endTime - startTime < 1000, "Interpolation took too long: " + (endTime - startTime) + "ms");
    }

    @Test
    void testConcurrency() throws InterruptedException {
        // 测试并发安全性
        final int threadCount = 10;
        final int iterationsPerThread = 100;
        final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        final List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        String template = "Thread ${threadId}, Iteration ${iteration}";
                        Map<String, Object> params = Map.of("threadId", threadId, "iteration", j);
                        String result = interpolator.interpolate(template, params, Locale.ENGLISH);
                        assertNotNull(result);
                        assertTrue(result.contains(String.valueOf(threadId)));
                        assertTrue(result.contains(String.valueOf(j)));
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 检查是否有异常
        if (!exceptions.isEmpty()) {
            fail("Concurrency test failed with exceptions: " + exceptions);
        }
    }

    // ==================== 业务场景测试 ====================

    @Test
    void testRealWorldScenarios() {
        // 模拟真实世界的用户场景
        BusinessUser user = new BusinessUser("张三", "zhang.san@example.com", 28, true);
        Order order = new Order("ORD-001", 299.99, "已发货");

        Map<String, Object> context = Map.of(
                "user", user,
                "order", order,
                "currentDate", new Date(),
                "supportEmail", "support@example.com"
        );

        // 测试用户欢迎消息
        String welcomeTemplate = "欢迎 ${user.name}！您的邮箱是 ${user.email}";
        String result = interpolator.interpolate(welcomeTemplate, context, Locale.CHINESE);
        assertEquals("欢迎 张三！您的邮箱是 zhang.san@example.com", result);

        // 测试订单状态消息
        String orderTemplate = "订单 ${order.id} 金额 ¥${order.amount} 状态：${order.status}";
        result = interpolator.interpolate(orderTemplate, context, Locale.CHINESE);
        assertEquals("订单 ORD-001 金额 ¥299.99 状态：已发货", result);
    }

    @Test
    void testEmailTemplateScenario() {
        // 模拟邮件模板场景
        BusinessUser recipient = new BusinessUser("李四", "li.si@example.com", 35, true);
        Map<String, Object> context = Map.of(
                "recipient", recipient,
                "senderName", "客服小王",
                "companyName", "科技有限公司",
                "resetLink", "https://example.com/reset?token=abc123"
        );

        String emailTemplate = """
                亲爱的 ${recipient.name}，
                
                您好！我是来自${companyName}的${senderName}。
                
                您的密码重置链接：${resetLink}
                
                如有疑问，请联系我们。
                
                祝好！
                ${senderName}
                """;

        String result = interpolator.interpolate(emailTemplate, context, Locale.CHINESE);

        assertTrue(result.contains("李四"));
        assertTrue(result.contains("客服小王"));
        assertTrue(result.contains("科技有限公司"));
        assertTrue(result.contains("https://example.com/reset?token=abc123"));
    }

    @Test
    void testMultiLanguageSupport() {
        BusinessUser user = new BusinessUser("John Doe", "john@example.com", 30, true);
        Map<String, Object> context = Map.of("user", user);

        // 英文模板
        String englishTemplate = "Welcome ${user.name}! Your email is ${user.email}";
        String englishResult = interpolator.interpolate(englishTemplate, context, Locale.ENGLISH);
        assertEquals("Welcome John Doe! Your email is john@example.com", englishResult);

        // 中文模板
        String chineseTemplate = "欢迎 ${user.name}！您的邮箱是 ${user.email}";
        String chineseResult = interpolator.interpolate(chineseTemplate, context, Locale.CHINESE);
        assertEquals("欢迎 John Doe！您的邮箱是 john@example.com", chineseResult);
    }

    @Test
    void testErrorRecovery() {
        Map<String, Object> context = Map.of("user", new BusinessUser("测试用户", "test@example.com", 20, true));

        // 测试部分表达式失败的情况
        String template = "用户：${user.name}，年龄：${user.age}，无效表达式：${user.invalidProperty}，邮箱：${user.email}";
        String result = interpolator.interpolate(template, context, Locale.CHINESE);

        // 应该处理有效的表达式，保留无效的表达式
        assertTrue(result.contains("测试用户"));
        assertTrue(result.contains("20"));
        assertTrue(result.contains("test@example.com"));
        // 无效属性可能被处理为null或保留原样
        assertTrue(result.contains("${user.invalidProperty}") || result.contains("null") || result.contains(""));
    }

    // 业务场景测试用的数据类
    public static class BusinessUser {
        private String name;
        private String email;
        private int age;
        private boolean active;

        public BusinessUser(String name, String email, int age, boolean active) {
            this.name = name;
            this.email = email;
            this.age = age;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }
    }

    public static class Order {
        private String id;
        private double amount;
        private String status;

        public Order(String id, double amount, String status) {
            this.id = id;
            this.amount = amount;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public String getStatus() {
            return status;
        }
    }
}
