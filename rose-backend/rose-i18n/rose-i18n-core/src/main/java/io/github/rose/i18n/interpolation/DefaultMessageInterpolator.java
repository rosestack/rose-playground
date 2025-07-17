package io.github.rose.i18n.interpolation;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认消息插值器实现
 *
 * <p>支持MessageFormat风格、命名参数风格和表达式的插值。
 * 这是一个组合插值器，会根据模板内容自动选择合适的插值方式。</p>
 *
 * <p>表达式插值支持可插拔的评估器：</p>
 * <ul>
 *   <li>Jakarta EL评估器（如果容器提供EL实现）</li>
 *   <li>简单表达式评估器（默认，无依赖）</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class DefaultMessageInterpolator implements MessageInterpolator {

    private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{\\d+\\}");
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final List<ExpressionEvaluator> expressionEvaluators;

    /**
     * 默认构造函数，自动发现可用的表达式评估器
     */
    public DefaultMessageInterpolator() {
        this.expressionEvaluators = discoverExpressionEvaluators();
    }

    /**
     * 构造函数
     *
     * @param expressionEvaluators 表达式评估器列表
     */
    public DefaultMessageInterpolator(List<ExpressionEvaluator> expressionEvaluators) {
        this.expressionEvaluators = new ArrayList<>(expressionEvaluators);
        // 按优先级排序
        this.expressionEvaluators.sort(Comparator.comparingInt(ExpressionEvaluator::getPriority));
    }

    @Override
    public String interpolate(String template, Object[] args, Locale locale) {
        if (template == null || !needsInterpolation(template)) {
            return template;
        }

        // 优先检查表达式
        if (EXPRESSION_PATTERN.matcher(template).find()) {
            return interpolateExpressions(template, args, locale);
        }

        // 然后检查MessageFormat
        if (MESSAGE_FORMAT_PATTERN.matcher(template).find()) {
            if (args == null || args.length == 0) {
                return template;
            }
            try {
                MessageFormat messageFormat = new MessageFormat(template, locale);
                return messageFormat.format(args);
            } catch (Exception e) {
                return template;
            }
        }

        return template;
    }

    @Override
    public String interpolate(String template, Map<String, Object> namedArgs, Locale locale) {
        if (template == null || !needsInterpolation(template)) {
            return template;
        }

        // 优先检查表达式
        if (EXPRESSION_PATTERN.matcher(template).find()) {
            return interpolateExpressions(template, namedArgs, locale);
        }

        // 然后检查命名参数
        if (NAMED_PARAMETER_PATTERN.matcher(template).find()) {
            if (namedArgs == null || namedArgs.isEmpty()) {
                return template;
            }

            String result = template;
            Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(template);

            while (matcher.find()) {
                String paramName = matcher.group(1);
                Object paramValue = namedArgs.get(paramName);

                if (paramValue != null) {
                    String replacement = formatValue(paramValue, locale);
                    result = result.replace("{" + paramName + "}", replacement);
                }
            }
            return result;
        }

        return template;
    }

    @Override
    public boolean needsInterpolation(String template) {
        if (template == null) {
            return false;
        }

        return MESSAGE_FORMAT_PATTERN.matcher(template).find() ||
               NAMED_PARAMETER_PATTERN.matcher(template).find() ||
               EXPRESSION_PATTERN.matcher(template).find();
    }

    @Override
    public String getName() {
        return "DefaultMessageInterpolator";
    }

    @Override
    public Set<InterpolationSyntax> getSupportedSyntax() {
        return Set.of(
            InterpolationSyntax.MESSAGE_FORMAT,
            InterpolationSyntax.NAMED_PARAMETERS,
            InterpolationSyntax.EXPRESSION
        );
    }

    /**
     * 格式化参数值
     * 
     * @param value 参数值
     * @param locale 语言环境
     * @return 格式化后的字符串
     */
    private String formatValue(Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Number) {
            return java.text.NumberFormat.getInstance(locale).format(value);
        }

        if (value instanceof java.util.Date) {
            return java.text.DateFormat.getDateTimeInstance(
                java.text.DateFormat.DEFAULT, 
                java.text.DateFormat.DEFAULT, 
                locale
            ).format(value);
        }

        if (value instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) value).format(
                java.time.format.DateTimeFormatter.ofLocalizedDateTime(
                    java.time.format.FormatStyle.MEDIUM
                ).withLocale(locale)
            );
        }

        if (value instanceof java.time.LocalDate) {
            return ((java.time.LocalDate) value).format(
                java.time.format.DateTimeFormatter.ofLocalizedDate(
                    java.time.format.FormatStyle.MEDIUM
                ).withLocale(locale)
            );
        }

        return value.toString();
    }

    /**
     * 自动发现可用的表达式评估器
     *
     * @return 表达式评估器列表
     */
    private List<ExpressionEvaluator> discoverExpressionEvaluators() {
        List<ExpressionEvaluator> evaluators = new ArrayList<>();

        // 尝试Jakarta EL（由容器提供实现）
        try {
            ExpressionEvaluator jakartaEL = new JakartaElExpressionEvaluator();
            if (jakartaEL.isAvailable()) {
                evaluators.add(jakartaEL);
            }
        } catch (Exception e) {
            // Jakarta EL不可用（容器未提供EL实现）
        }

        // 添加简单表达式评估器作为后备
        evaluators.add(new SimpleExpressionEvaluator());

        // 按优先级排序
        evaluators.sort(Comparator.comparingInt(ExpressionEvaluator::getPriority));

        return evaluators;
    }

    /**
     * 插值表达式
     *
     * @param template 模板
     * @param args 数组参数
     * @param locale 语言环境
     * @return 插值结果
     */
    private String interpolateExpressions(String template, Object[] args, Locale locale) {
        // 将数组参数转换为命名参数
        Map<String, Object> namedArgs = new HashMap<>();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                namedArgs.put("arg" + i, args[i]);
                namedArgs.put(String.valueOf(i), args[i]); // 同时支持数字索引
            }
        }

        return interpolateExpressions(template, namedArgs, locale);
    }

    /**
     * 插值表达式
     *
     * @param template 模板
     * @param namedArgs 命名参数
     * @param locale 语言环境
     * @return 插值结果
     */
    private String interpolateExpressions(String template, Map<String, Object> namedArgs, Locale locale) {
        if (template == null) {
            return null;
        }

        // 查找并替换所有表达式
        Matcher matcher = EXPRESSION_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String expression = matcher.group(1);
            String replacement = evaluateExpression(expression, namedArgs, locale);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 评估表达式
     *
     * @param expression 表达式内容
     * @param variables 变量映射
     * @param locale 语言环境
     * @return 评估结果的字符串表示
     */
    private String evaluateExpression(String expression, Map<String, Object> variables, Locale locale) {
        // 尝试每个评估器
        for (ExpressionEvaluator evaluator : expressionEvaluators) {
            if (evaluator.supports(expression)) {
                try {
                    Object result = evaluator.evaluate(expression, variables, locale);
                    return result != null ? result.toString() : "";
                } catch (Exception e) {
                    // 继续尝试下一个评估器
                }
            }
        }

        // 如果所有评估器都失败，返回原始表达式
        return "${" + expression + "}";
    }
}
