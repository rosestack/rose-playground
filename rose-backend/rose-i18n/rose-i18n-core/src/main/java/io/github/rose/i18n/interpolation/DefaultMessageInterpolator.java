package io.github.rose.i18n.interpolation;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.interpolation.evaluator.ExpressionEvaluator;
import io.github.rose.i18n.interpolation.evaluator.SimpleExpressionEvaluator;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultMessageInterpolator implements MessageInterpolator {
    private static final Pattern PLACE_HODLER_PATTERN = Pattern.compile("\\{\\}");
    private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{\\d+\\}");
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private volatile ExpressionEvaluator expressionEvaluator = new SimpleExpressionEvaluator();

    public DefaultMessageInterpolator() {
    }

    public DefaultMessageInterpolator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator != null ? expressionEvaluator : new SimpleExpressionEvaluator();
    }

    public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator != null ? expressionEvaluator : new SimpleExpressionEvaluator();
    }

    @Override
    public String interpolate(String message, Object[] args, Locale locale) {
        if (message == null) {
            return null;
        }
        if (args == null) {
            return message;
        }

        // 检测模板中使用的格式类型
        boolean hasMessageFormat = MESSAGE_FORMAT_PATTERN.matcher(message).find();
        boolean hasFormat = PLACE_HODLER_PATTERN.matcher(message).find();

        // MessageFormat style - 优先级更高
        if (hasMessageFormat) {
            try {
                MessageFormat messageFormat = new MessageFormat(message, locale);
                return messageFormat.format(args);
            } catch (Exception e) {
                // 如果 MessageFormat 失败，回退到 Format 风格
            }
        }

        // Format style
        if (hasFormat) {
            return FormatUtils.format(message, args);
        }

        // 如果都没有匹配的格式，返回原消息
        return message;
    }

    @Override
    public String interpolate(String message, Map<String, Object> args, Locale locale) {
        if (message == null) {
            return null;
        }
        if (args == null || args.isEmpty()) {
            return message;
        }

        // 检测模板中使用的格式类型
        boolean hasExpression = EXPRESSION_PATTERN.matcher(message).find();
        boolean hasNamedParameter = NAMED_PARAMETER_PATTERN.matcher(message).find();

        // 表达式风格 - 优先级更高
        if (hasExpression) {
            return interpolateExpressions(message, args, locale);
        }

        // 命名参数风格
        if (hasNamedParameter) {
            return interpolateNamedParameters(message, args, locale);
        }

        // 如果都没有匹配的格式，返回原消息
        return message;
    }

    private String interpolateNamedParameters(String template, Map<String, Object> namedArgs, Locale locale) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(template);
        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(template, lastEnd, matcher.start());
            String paramName = matcher.group(1);
            Object paramValue = namedArgs.get(paramName);
            String replacement = paramValue != null ? formatValue(paramValue, locale) : matcher.group(0);
            sb.append(replacement);
            lastEnd = matcher.end();
        }
        sb.append(template.substring(lastEnd));
        return sb.toString();
    }

    private String interpolateExpressions(String template, Map<String, Object> namedArgs, Locale locale) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String expression = matcher.group(1);
            if (expression != null) {
                String replacement = evaluateExpression(expression, namedArgs, locale);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement != null ? replacement : ""));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String evaluateExpression(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return "";
        }

        if (expressionEvaluator != null) {
            try {
                Object result = expressionEvaluator.evaluate(expression, variables, locale);
                return result != null ? result.toString() : "";
            } catch (Exception e) {
                // ignore and return original expression
            }
        }
        return "${" + expression + "}";
    }

    private String formatValue(Object value, Locale locale) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number) {
            // Support locale-aware formatting, extensible pattern
            NumberFormat nf = NumberFormat.getInstance(locale);
            return nf.format(value);
        }
        if (value instanceof java.util.Date) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
            return df.format(value);
        }
        if (value instanceof java.time.LocalDateTime) {
            DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
            return ((java.time.LocalDateTime) value).format(dtf);
        }
        if (value instanceof java.time.LocalDate) {
            DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            return ((java.time.LocalDate) value).format(dtf);
        }
        return value.toString();
    }

    /**
     * Static factory method, returns default implementation
     */
    public static DefaultMessageInterpolator create() {
        return new DefaultMessageInterpolator();
    }

    /**
     * Static factory method, uses specified expression evaluator
     */
    public static DefaultMessageInterpolator create(ExpressionEvaluator expressionEvaluator) {
        return new DefaultMessageInterpolator(expressionEvaluator);
    }
}
