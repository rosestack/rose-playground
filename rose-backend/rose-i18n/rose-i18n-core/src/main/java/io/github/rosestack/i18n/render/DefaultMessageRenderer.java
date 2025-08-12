package io.github.rosestack.i18n.render;

import io.github.rosestack.core.util.FormatUtils;
import io.github.rosestack.i18n.evaluator.ExpressionEvaluator;
import io.github.rosestack.i18n.evaluator.SpelExpressionEvaluator;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultMessageRenderer implements MessageRenderer {
    private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{\\d+\\}");
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final ExpressionEvaluator expressionEvaluator;

    public DefaultMessageRenderer() {
        this(new SpelExpressionEvaluator());
    }

    public DefaultMessageRenderer(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public String render(String message, Locale locale, Object args) {
        if (message == null) {
            return null;
        }

        if (args == null) {
            return message;  // null 参数时返回原消息
        }

        // 根据参数类型和消息格式选择最合适的插值方式，只处理一次
        if (args instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapArgs = (Map<String, Object>) args;

            if (EXPRESSION_PATTERN.matcher(message).find()) {
                return processExpressions(message, mapArgs, locale);
            }

            // 处理 {name} 命名参数
            if (NAMED_PARAMETER_PATTERN.matcher(message).find()) {
                return FormatUtils.replaceNamedParameters(message, locale, mapArgs);
            }
        }

        if (args instanceof Object[]) {
            Object[] arrayArgs = (Object[]) args;

            // 处理 {0}, {1} MessageFormat 风格
            if (MESSAGE_FORMAT_PATTERN.matcher(message).find()) {
                try {
                    MessageFormat messageFormat = new MessageFormat(message, locale);
                    return messageFormat.format(arrayArgs);
                } catch (Exception e) {
                }
            }

            // 处理 {} 占位符
            if (message.contains("{}")) {
                return FormatUtils.replacePlaceholders(message, arrayArgs);
            }
        }

        // 处理 {} 占位符
        if (message.contains("{}")) {
            return FormatUtils.replacePlaceholders(message, args);
        }

        // 如果没有匹配的格式，返回原消息
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

            if (!expressionEvaluator.supports(expression)) {
                return message;
            } else {
                try {
                    Object value = expressionEvaluator.evaluate(expression, arg, locale);
                    if (value != null) {
                        replacement = value.toString();
                    } else {
                        replacement = "null";
                    }
                } catch (Exception e) {
                    replacement = matcher.group(0); // 保持原样
                }
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}


