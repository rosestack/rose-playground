package io.github.rosestack.i18n.evaluator;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spring Expression Language (SpEL) 表达式评估器
 *
 * <p>基于Spring的SpEL引擎，提供强大的表达式评估能力。
 *
 * @author rose
 * @since 1.0.0
 */
@Slf4j
public class SpelExpressionEvaluator implements ExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final Pattern SPEL_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Override
    public Object evaluate(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 设置变量
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }

            // 注入locale
            if (locale != null) {
                context.setVariable("locale", locale);
                context.setVariable("language", locale.getLanguage());
                context.setVariable("country", locale.getCountry());
                context.setVariable("displayLanguage", locale.getDisplayLanguage(locale));
                context.setVariable("displayCountry", locale.getDisplayCountry(locale));
            }

            // 注入一些常用的工具类
            context.setVariable("T", java.lang.Math.class);
            context.setVariable("Arrays", java.util.Arrays.class);
            context.setVariable("Collections", java.util.Collections.class);

            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue(context);

            if (log.isDebugEnabled()) {
                log.debug("SpEL evaluation: {} -> {}", expression, result);
            }

            return result;

        } catch (Exception e) {
            log.debug("SpEL evaluation failed for expression: {}", expression, e);
            return null;
        }
    }

    @Override
    public boolean supports(String template) {
        return template != null && SPEL_PATTERN.matcher(template).find();
    }
}
