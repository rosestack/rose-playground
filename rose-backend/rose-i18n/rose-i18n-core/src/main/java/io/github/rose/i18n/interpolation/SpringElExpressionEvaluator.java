package io.github.rose.i18n.interpolation;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Locale;
import java.util.Map;

/**
 * 基于 Spring Expression Language (SpEL) 的表达式评估器
 */
public class SpringElExpressionEvaluator implements ExpressionEvaluator {
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }
            // 可选：注入 locale
            if (locale != null) {
                context.setVariable("locale", locale);
            }
            Expression exp = parser.parseExpression(expression);
            return exp.getValue(context);
        } catch (Throwable e) {
            // Spring Expression 依赖不可用或表达式错误
            return null;
        }
    }
} 