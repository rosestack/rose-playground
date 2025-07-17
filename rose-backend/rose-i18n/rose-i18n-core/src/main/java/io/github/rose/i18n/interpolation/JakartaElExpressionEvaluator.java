package io.github.rose.i18n.interpolation;

import jakarta.el.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Jakarta EL表达式评估器
 *
 * <p>基于Jakarta EL API的表达式评估器实现，支持完整的EL表达式语法。
 * 具体的EL实现由容器提供。</p>
 *
 * <p>支持的语法：</p>
 * <ul>
 *   <li>属性访问：user.name, user.profile.email</li>
 *   <li>方法调用：user.getName(), user.getAge()</li>
 *   <li>条件表达式：user.age >= 18 ? 'adult' : 'minor'</li>
 *   <li>算术运算：price * quantity, total + tax</li>
 *   <li>逻辑运算：user.active && user.verified</li>
 *   <li>集合操作：users[0].name, map['key']</li>
 *   <li>空值检查：user.name != null ? user.name : 'Anonymous'</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class JakartaElExpressionEvaluator implements ExpressionEvaluator {

    private final ExpressionFactory expressionFactory;

    /**
     * 默认构造函数
     */
    public JakartaElExpressionEvaluator() {
        try {
            this.expressionFactory = ExpressionFactory.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Jakarta EL not available", e);
        }
    }

    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            // 创建可变的参数映射
            Map<String, Object> mutableArgs = new HashMap<>();
            if (variables != null) {
                mutableArgs.putAll(variables);
            }

            // 添加locale参数（如果没有的话）
            if (!mutableArgs.containsKey("locale")) {
                mutableArgs.put("locale", locale);
            }

            // 创建EL上下文
            ELContext elContext = createELContext(mutableArgs);

            // 评估表达式
            return evaluateExpression(expression.trim(), elContext);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 创建EL上下文
     * 
     * @param variables 变量映射
     * @return EL上下文
     */
    private ELContext createELContext(Map<String, Object> variables) {
        return new SimpleELContext(expressionFactory, variables);
    }

    /**
     * 评估EL表达式
     *
     * @param expression EL表达式（不包含${}）
     * @param elContext EL上下文
     * @return 评估结果
     */
    private Object evaluateExpression(String expression, ELContext elContext) {
        try {
            ValueExpression valueExpression = getValueExpression(expression, elContext);
            return valueExpression.getValue(elContext);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取值表达式
     *
     * @param expression EL表达式
     * @param elContext EL上下文
     * @return 值表达式
     */
    private ValueExpression getValueExpression(String expression, ELContext elContext) {
        String fullExpression = "${" + expression + "}";
        return expressionFactory.createValueExpression(elContext, fullExpression, Object.class);
    }
    /**
     * 简单的EL上下文实现
     */
    private static class SimpleELContext extends ELContext {
        private final ExpressionFactory expressionFactory;
        private final VariableMapper variableMapper;
        private final FunctionMapper functionMapper;
        private final ELResolver elResolver;

        public SimpleELContext(ExpressionFactory expressionFactory, Map<String, Object> variables) {
            this.expressionFactory = expressionFactory;
            this.variableMapper = new SimpleVariableMapper();
            this.functionMapper = new SimpleFunctionMapper();
            this.elResolver = new CompositeELResolver();
            
            // 添加标准解析器
            ((CompositeELResolver) elResolver).add(new ArrayELResolver());
            ((CompositeELResolver) elResolver).add(new ListELResolver());
            ((CompositeELResolver) elResolver).add(new MapELResolver());
            ((CompositeELResolver) elResolver).add(new ResourceBundleELResolver());
            ((CompositeELResolver) elResolver).add(new BeanELResolver());
            
            // 设置变量
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                ValueExpression valueExpression = expressionFactory.createValueExpression(
                    entry.getValue(), Object.class);
                variableMapper.setVariable(entry.getKey(), valueExpression);
            }
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
     * 简单的变量映射器实现
     */
    private static class SimpleVariableMapper extends VariableMapper {
        private final Map<String, ValueExpression> variables = new HashMap<>();

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
     * 简单的函数映射器实现
     */
    private static class SimpleFunctionMapper extends FunctionMapper {
        @Override
        public Method resolveFunction(String prefix, String localName) {
            // 可以在这里添加自定义函数
            return null;
        }
    }
}
