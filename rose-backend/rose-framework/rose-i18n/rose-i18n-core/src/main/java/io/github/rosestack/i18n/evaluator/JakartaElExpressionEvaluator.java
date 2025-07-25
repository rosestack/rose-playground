package io.github.rosestack.i18n.evaluator;

import jakarta.el.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 高性能Jakarta EL表达式评估器
 *
 * <p>基于Jakarta EL API的表达式评估器实现，支持完整的EL表达式语法。
 * 包含缓存机制和自定义函数支持。</p>
 *
 * @author chensoul
 * @since 1.0.0
 */
public class JakartaElExpressionEvaluator implements ExpressionEvaluator {

    private final ExpressionFactory expressionFactory;
    private final Map<String, ValueExpression> expressionCache = new ConcurrentHashMap<>();
    private final Map<String, Function<Object[], Object>> customFunctions = new ConcurrentHashMap<>();

    private boolean cacheEnabled = true;
    private boolean available = false;

    /**
     * 默认构造函数
     */
    public JakartaElExpressionEvaluator() {
        ExpressionFactory factory = null;
        try {
            factory = ExpressionFactory.newInstance();
            this.available = true;
        } catch (Exception e) {
            // Jakarta EL 不可用，但不抛出异常
            this.available = false;
        }
        this.expressionFactory = factory;
    }

    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        // 如果 Jakarta EL 不可用，直接返回 null
        if (!available || expressionFactory == null) {
            return null;
        }

        try {
            String trimmedExpression = expression.trim();

            // 检查缓存
            if (cacheEnabled) {
                ValueExpression cachedExpression = expressionCache.get(trimmedExpression);
                if (cachedExpression != null) {
                    return evaluateCachedExpression(cachedExpression, variables, locale);
                }
            }

            // 创建可变的参数映射
            Map<String, Object> mutableArgs = new HashMap<>();
            if (variables != null) {
                mutableArgs.putAll(variables);
            }

            // 添加locale参数
            if (locale != null && !mutableArgs.containsKey("locale")) {
                mutableArgs.put("locale", locale);
            }

            // 创建EL上下文
            ELContext elContext = createELContext(mutableArgs);

            // 评估表达式
            ValueExpression valueExpression = getValueExpression(trimmedExpression, elContext);
            Object result = valueExpression.getValue(elContext);

            // 缓存表达式
            if (cacheEnabled) {
                expressionCache.put(trimmedExpression, valueExpression);
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supports(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        // 如果 Jakarta EL 不可用，不支持任何表达式
        if (!available || expressionFactory == null) {
            return false;
        }

        // Jakarta EL支持所有有效的EL表达式
        return true;
    }

    /**
     * 评估缓存的表达式
     */
    private Object evaluateCachedExpression(ValueExpression expression, Map<String, Object> variables, Locale locale) {
        try {
            Map<String, Object> mutableArgs = new HashMap<>(variables);
            if (locale != null && !mutableArgs.containsKey("locale")) {
                mutableArgs.put("locale", locale);
            }

            ELContext elContext = createELContext(mutableArgs);
            return expression.getValue(elContext);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 创建EL上下文
     */
    private ELContext createELContext(Map<String, Object> variables) {
        return new OptimizedELContext(expressionFactory, variables, customFunctions);
    }

    /**
     * 获取值表达式
     */
    private ValueExpression getValueExpression(String expression, ELContext elContext) {
        String fullExpression = "${" + expression + "}";
        return expressionFactory.createValueExpression(elContext, fullExpression, Object.class);
    }

    /**
     * 优化的EL上下文实现
     */
    private static class OptimizedELContext extends ELContext {
        private final ExpressionFactory expressionFactory;
        private final VariableMapper variableMapper;
        private final FunctionMapper functionMapper;
        private final ELResolver elResolver;

        public OptimizedELContext(ExpressionFactory expressionFactory, Map<String, Object> variables,
                                  Map<String, Function<Object[], Object>> customFunctions) {
            this.expressionFactory = expressionFactory;
            this.variableMapper = new OptimizedVariableMapper(variables, expressionFactory);
            this.functionMapper = new CustomFunctionMapper(customFunctions);
            this.elResolver = createOptimizedResolver();
        }

        private ELResolver createOptimizedResolver() {
            CompositeELResolver resolver = new CompositeELResolver();

            // 按性能顺序添加解析器
            resolver.add(new ArrayELResolver());
            resolver.add(new ListELResolver());
            resolver.add(new MapELResolver());
            resolver.add(new ResourceBundleELResolver());
            resolver.add(new BeanELResolver());

            return resolver;
        }

        @Override
        public ELResolver getELResolver() {
            return elResolver;
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return functionMapper;
        }

        @Override
        public VariableMapper getVariableMapper() {
            return variableMapper;
        }
    }

    /**
     * 优化的变量映射器
     */
    private static class OptimizedVariableMapper extends VariableMapper {
        private final Map<String, ValueExpression> variables = new HashMap<>();

        public OptimizedVariableMapper(Map<String, Object> variables, ExpressionFactory factory) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                ValueExpression valueExpression = factory.createValueExpression(
                        entry.getValue(), Object.class);
                this.variables.put(entry.getKey(), valueExpression);
            }
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            return variables.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return variables.put(variable, expression);
        }
    }

    /**
     * 自定义函数映射器
     */
    private static class CustomFunctionMapper extends FunctionMapper {
        private final Map<String, Function<Object[], Object>> customFunctions;

        public CustomFunctionMapper(Map<String, Function<Object[], Object>> customFunctions) {
            this.customFunctions = customFunctions;
        }

        @Override
        public Method resolveFunction(String prefix, String localName) {
            // 这里可以返回自定义函数的Method对象
            // 或者通过其他方式集成自定义函数
            return null;
        }
    }
}