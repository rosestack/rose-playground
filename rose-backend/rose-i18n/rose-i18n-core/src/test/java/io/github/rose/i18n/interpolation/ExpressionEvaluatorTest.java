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
        
        assertTrue(evaluator.isAvailable());
        assertEquals("SimpleExpressionEvaluator", evaluator.getName());
        assertEquals(100, evaluator.getPriority()); // 低优先级
        
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
        
        // 测试不支持的复杂表达式
        assertFalse(evaluator.supports("user.age >= 18 ? 'adult' : 'minor'"));
        assertTrue(evaluator.supports("user.name"));
        assertTrue(evaluator.supports("user.getName()"));
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
        
        if (!evaluator.isAvailable()) {
            return; // 跳过测试
        }
        
        assertEquals("JakartaElExpressionEvaluator", evaluator.getName());
        assertEquals(10, evaluator.getPriority()); // 高优先级
        
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
        
        // 测试简单表达式（应该使用SimpleExpressionEvaluator）
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
    void testExpressionEvaluatorPriority() {
        List<ExpressionEvaluator> evaluators = new ArrayList<>();
        
        // 添加简单评估器
        evaluators.add(new SimpleExpressionEvaluator());
        
        // 尝试添加Jakarta EL评估器
        try {
            ExpressionEvaluator jakartaEL = new JakartaElExpressionEvaluator();
            if (jakartaEL.isAvailable()) {
                evaluators.add(jakartaEL);
            }
        } catch (Exception e) {
            // 忽略
        }
        

        
        // 按优先级排序
        evaluators.sort(Comparator.comparingInt(ExpressionEvaluator::getPriority));
        
        // 验证排序结果
        assertTrue(evaluators.size() >= 1); // 至少有SimpleExpressionEvaluator
        
        // 最后一个应该是SimpleExpressionEvaluator（优先级最低）
        ExpressionEvaluator lastEvaluator = evaluators.get(evaluators.size() - 1);
        assertEquals("SimpleExpressionEvaluator", lastEvaluator.getName());
        assertEquals(100, lastEvaluator.getPriority());
    }

    @Test
    void testCacheStatistics() {
        SimpleExpressionEvaluator evaluator = new SimpleExpressionEvaluator();
        
        ExpressionEvaluator.CacheStatistics stats = evaluator.getCacheStatistics();
        assertEquals(0, stats.getSize());
        assertEquals(0, stats.getHitCount());
        assertEquals(0, stats.getMissCount());
        assertEquals(0.0, stats.getHitRate());
        
        // 执行一些评估来产生缓存统计
        TestUser user = new TestUser("John", 25);
        Map<String, Object> variables = Map.of("user", user);
        
        evaluator.evaluate("user.getName()", variables, Locale.ENGLISH);
        evaluator.evaluate("user.getName()", variables, Locale.ENGLISH); // 应该命中缓存
        
        stats = evaluator.getCacheStatistics();
        assertTrue(stats.getHitCount() > 0 || stats.getMissCount() > 0);
        
        // 清除缓存
        evaluator.clearCache();
        stats = evaluator.getCacheStatistics();
        assertEquals(0, stats.getSize());
    }

    @Test
    void testExpressionSupport() {
        SimpleExpressionEvaluator simpleEvaluator = new SimpleExpressionEvaluator();
        
        // 简单表达式应该被支持
        assertTrue(simpleEvaluator.supports("user.name"));
        assertTrue(simpleEvaluator.supports("user.getName()"));
        assertTrue(simpleEvaluator.supports("users[0].name"));
        assertTrue(simpleEvaluator.supports("config['key']"));
        
        // 复杂表达式不应该被支持
        assertFalse(simpleEvaluator.supports("user.age >= 18 ? 'adult' : 'minor'"));
        assertFalse(simpleEvaluator.supports("price * quantity"));
        assertFalse(simpleEvaluator.supports("user.active && user.verified"));
    }

    // 测试用的辅助类
    public static class TestUser {
        private String name;
        private int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }
}
