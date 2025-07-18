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
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{\\}");
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
    public String interpolate(String template, Object args, Locale locale) {
        if (template == null) {
            return null;
        }
        if (args == null) {
            return template;
        }

        // Priority: Expression style
        if (EXPRESSION_PATTERN.matcher(template).find()) {
            if (args instanceof Object[]) {
                return interpolateExpressions(template, (Object[]) args, locale);
            } else if (args instanceof Map) {
                Map<String, Object> mapArgs = (Map<String, Object>) args;
                return interpolateExpressions(template, mapArgs, locale);
            } else {
                return template;
            }
        }

        // MessageFormat style
        if (MESSAGE_FORMAT_PATTERN.matcher(template).find()) {
            if (args instanceof Object[] && ((Object[]) args).length > 0) {
                try {
                    MessageFormat messageFormat = new MessageFormat(template, locale);
                    return messageFormat.format(args);
                } catch (Exception e) {
                    return template;
                }
            } else {
                return template;
            }
        }

        // Named parameter style
        if (NAMED_PARAMETER_PATTERN.matcher(template).find() && args instanceof Map) {
            Map<String, Object> mapArgs = (Map<String, Object>) args;
            return interpolateNamedParameters(template, mapArgs, locale);
        }

        if (FORMAT_PATTERN.matcher(template).find() && args instanceof Object[]) {
            Object[] objArgs = (Object[]) args;
            return FormatUtils.format(template, objArgs);
        }

        return template;
    }

    private String interpolateNamedParameters(String template, Map<String, Object> namedArgs, Locale locale) {
        if (template == null) {
            return null;
        }
        if (namedArgs == null || namedArgs.isEmpty()) {
            return template;
        }

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

    private String interpolateExpressions(String template, Object[] args, Locale locale) {
        Map<String, Object> namedArgs = new HashMap<>();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                namedArgs.put("arg" + i, args[i]);
                namedArgs.put(String.valueOf(i), args[i]);
            }
        }
        return interpolateExpressions(template, namedArgs, locale);
    }

    private String interpolateExpressions(String template, Map<String, Object> namedArgs, Locale locale) {
        if (template == null) {
            return null;
        }

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
