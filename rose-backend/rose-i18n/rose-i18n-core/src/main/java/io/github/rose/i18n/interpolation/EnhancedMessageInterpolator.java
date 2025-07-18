package io.github.rose.i18n.interpolation;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.interpolation.evaluator.ExpressionEvaluator;
import io.github.rose.i18n.interpolation.evaluator.JakartaElExpressionEvaluator;
import io.github.rose.i18n.interpolation.evaluator.SimpleExpressionEvaluator;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 增强的消息插值器
 * <p>
 * 支持多种表达式语法和缓存机制，提供高性能的消息模板处理。
 * 支持以下格式：
 * - {} 占位符：FormatUtils.format() 风格
 * - {0}, {1} 索引：MessageFormat 风格
 * - {name} 命名参数：命名参数风格
 * - ${expression} 表达式：表达式评估风格
 *
 * @author rose
 * @since 1.0.0
 */
public class EnhancedMessageInterpolator implements MessageInterpolator {

    // 正则表达式模式
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{\\}");
    private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{\\d+\\}");
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final Map<String, ExpressionEvaluator> evaluators = new ConcurrentHashMap<>();
    private final Map<String, Object> expressionCache = new ConcurrentHashMap<>();
    private volatile ExpressionEvaluator defaultExpressionEvaluator = new SimpleExpressionEvaluator();

    public EnhancedMessageInterpolator() {
        // 注册默认评估器
        registerEvaluator("simple", new SimpleExpressionEvaluator());
        try {
            registerEvaluator("el", new JakartaElExpressionEvaluator());
        } catch (Exception e) {
            // Jakarta EL 不可用时忽略，只使用 SimpleExpressionEvaluator
        }
    }

    /**
     * 注册表达式评估器
     *
     * @param name      评估器名称
     * @param evaluator 评估器实例
     */
    public void registerEvaluator(String name, ExpressionEvaluator evaluator) {
        evaluators.put(name, evaluator);
        if (name.equals("simple")) {
            this.defaultExpressionEvaluator = evaluator;
        }
    }

    /**
     * 移除表达式评估器
     *
     * @param name 评估器名称
     */
    public void removeEvaluator(String name) {
        evaluators.remove(name);
        if (name.equals("simple")) {
            this.defaultExpressionEvaluator = new SimpleExpressionEvaluator();
        }
    }

    /**
     * 获取所有注册的评估器名称
     *
     * @return 评估器名称集合
     */
    public Set<String> getEvaluatorNames() {
        return new HashSet<>(evaluators.keySet());
    }

    @Override
    public String interpolate(String template, Object args, Locale locale) {
        if (template == null) {
            return null;
        }
        if (args == null) {
            return template;
        }

        // 缓存键
        String cacheKey = generateCacheKey(template, args, locale);

        // 尝试从缓存获取
        Object cached = expressionCache.get(cacheKey);
        if (cached != null) {
            return cached.toString();
        }

        // 执行插值
        String result = doInterpolate(template, args, locale);

        // 缓存结果
        expressionCache.put(cacheKey, result);

        return result;
    }

    /**
     * 执行插值处理，按优先级处理不同类型的占位符
     */
    private String doInterpolate(String template, Object args, Locale locale) {
        // 优先级1: 表达式风格 ${expression}
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

        // 优先级2: MessageFormat风格 {0}, {1}
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

        // 优先级3: 命名参数风格 {name}
        if (NAMED_PARAMETER_PATTERN.matcher(template).find() && args instanceof Map) {
            Map<String, Object> mapArgs = (Map<String, Object>) args;
            return interpolateNamedParameters(template, mapArgs, locale);
        }

        // 优先级4: 简单占位符风格 {}
        if (FORMAT_PATTERN.matcher(template).find() && args instanceof Object[]) {
            Object[] objArgs = (Object[]) args;
            return FormatUtils.format(template, objArgs);
        }

        return template;
    }

    /**
     * 插值表达式 ${expression}
     */
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

    /**
     * 插值表达式 ${expression}
     */
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

    /**
     * 插值命名参数 {name}
     */
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

    /**
     * 评估表达式
     */
    private String evaluateExpression(String expression, Map<String, Object> variables, Locale locale) {
        if (expression == null || expression.trim().isEmpty()) {
            return "";
        }

        // 尝试使用所有注册的评估器
        for (ExpressionEvaluator evaluator : evaluators.values()) {
            try {
                if (evaluator.supports(expression)) {
                    Object result = evaluator.evaluate(expression, variables, locale);
                    return result != null ? result.toString() : "";
                }
            } catch (Exception e) {
                // 继续尝试下一个评估器
            }
        }

        // 使用默认评估器
        try {
            Object result = defaultExpressionEvaluator.evaluate(expression, variables, locale);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            // 返回原始表达式
            return "${" + expression + "}";
        }
    }

    /**
     * 格式化值
     */
    private String formatValue(Object value, Locale locale) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number) {
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(locale);
            return nf.format(value);
        }
        if (value instanceof java.util.Date) {
            java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT, locale);
            return df.format(value);
        }
        if (value instanceof java.time.LocalDateTime) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter
                    .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM).withLocale(locale);
            return ((java.time.LocalDateTime) value).format(dtf);
        }
        if (value instanceof java.time.LocalDate) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter
                    .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(locale);
            return ((java.time.LocalDate) value).format(dtf);
        }
        return value.toString();
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String template, Object args, Locale locale) {
        return template + "|" + Objects.hash(args) + "|" + locale.toString();
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        expressionCache.clear();
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return expressionCache.size();
    }

    /**
     * 设置默认表达式评估器
     */
    public void setDefaultExpressionEvaluator(ExpressionEvaluator evaluator) {
        this.defaultExpressionEvaluator = evaluator != null ? evaluator : new SimpleExpressionEvaluator();
    }

    /**
     * 获取默认表达式评估器
     */
    public ExpressionEvaluator getDefaultExpressionEvaluator() {
        return defaultExpressionEvaluator;
    }

    /**
     * 静态工厂方法，返回默认实现
     */
    public static EnhancedMessageInterpolator create() {
        return new EnhancedMessageInterpolator();
    }

    /**
     * 静态工厂方法，使用指定的表达式评估器
     */
    public static EnhancedMessageInterpolator create(ExpressionEvaluator expressionEvaluator) {
        EnhancedMessageInterpolator interpolator = new EnhancedMessageInterpolator();
        interpolator.setDefaultExpressionEvaluator(expressionEvaluator);
        return interpolator;
    }
}