package io.github.rose.i18n.interpolation;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.interpolation.evaluator.ExpressionEvaluator;
import io.github.rose.i18n.interpolation.evaluator.SimpleExpressionEvaluator;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认消息插值器实现
 *
 * <p>根据参数类型自动选择合适的插值方式：</p>
 * <ul>
 *   <li>Object[] 或 Object...：使用 FormatUtils.format() 和 MessageFormat</li>
 *   <li>Map&lt;String, Object&gt;：使用命名参数和表达式插值</li>
 *   <li>其他类型：转换为字符串后处理</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class DefaultMessageInterpolator implements MessageInterpolator {
    private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{\\d+\\}");
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final ExpressionEvaluator expressionEvaluator;

    public DefaultMessageInterpolator() {
        this(new SimpleExpressionEvaluator());
    }

    public DefaultMessageInterpolator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public String interpolate(String message, Locale locale, Object arg) {
        if (message == null || arg == null) {
            return null;
        }

        // 1. 处理 ${expression} 表达式
        if (EXPRESSION_PATTERN.matcher(message).find()) {
            if (arg instanceof Map) {
                return processExpressions(message, (Map<String, Object>) arg, locale);
            }
        }

        // 2. 处理 {name} 命名参数
        if (NAMED_PARAMETER_PATTERN.matcher(message).find()) {
            if (arg instanceof Map) {
                return FormatUtils.formatVariables(message, (Map<String, Object>) arg);
            }
        }

        // 3. 处理 {0}, {1} MessageFormat 风格
        if (MESSAGE_FORMAT_PATTERN.matcher(message).find()) {
            if (arg instanceof Object[] && ((Object[]) arg).length > 0) {
                try {
                    MessageFormat messageFormat = new MessageFormat(message, locale);
                    return messageFormat.format(arg);
                } catch (Exception e) {
                    return message;
                }
            }

        }

        // 4. 处理 {} 占位符
        if (message.contains("{}")) {
            if (arg instanceof Object[] && ((Object[]) arg).length > 0) {
                return FormatUtils.format(message, arg);
            }
        }

        return message;
    }

    /**
     * 处理 ${expression} 表达式
     */
    private String processExpressions(String message, Map<String, Object> arg, Locale locale) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String expression = matcher.group(1);
            String replacement;

            try {
                Object value = expressionEvaluator.evaluate(expression, arg, locale);
                if (value != null) {
                    replacement = value.toString();
                } else {
                    replacement = matcher.group(0); // 保持原样
                }
            } catch (Exception e) {
                replacement = matcher.group(0); // 保持原样
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
