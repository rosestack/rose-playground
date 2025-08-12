package io.github.rosestack.core.util;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public abstract class FormatUtils {
    public static final String DEFAULT_PLACEHOLDER = "{}";
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final Pattern INDEXED_PARAMETER_PATTERN = Pattern.compile("\\{(\\d+)\\}");

    private FormatUtils() {}

    /**
     * 格式化占位符（使用默认占位符 {}）
     *
     * @param template 模板字符串
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    public static String replacePlaceholders(final String template, final Object... args) {
        return replacePlaceholders(template, Locale.getDefault(), TimeZone.getDefault(), args);
    }

    public static String replacePlaceholders(final String template, final Locale locale, final Object... args) {
        return replacePlaceholders(template, locale, TimeZone.getDefault(), args);
    }

    /**
     * 格式化占位符（使用默认占位符 {}，支持本地化）
     *
     * @param template 模板字符串
     * @param locale 本地化设置
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    public static String replacePlaceholders(
            final String template, final Locale locale, final TimeZone timeZone, final Object... args) {
        return replaceCustomPlaceholder(template, DEFAULT_PLACEHOLDER, locale, timeZone, args);
    }

    /**
     * 格式化占位符（自定义占位符）
     *
     * @param template 模板字符串
     * @param placeholder 占位符字符串
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    public static String replaceCustomPlaceholder(final String template, String placeholder, final Object... args) {
        return replaceCustomPlaceholder(template, placeholder, Locale.getDefault(), TimeZone.getDefault(), args);
    }

    /**
     * 格式化占位符（自定义占位符，支持本地化）
     *
     * @param template 模板字符串
     * @param placeholder 占位符字符串
     * @param locale 本地化设置
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    public static String replaceCustomPlaceholder(
            final String template, String placeholder, final Locale locale, TimeZone timeZone, final Object... args) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        String result = template;

        for (int i = 0; i < args.length; i++) {
            int index = result.indexOf(placeholder);
            if (index == -1) {
                break;
            }
            String value = LocaleFormatUtils.formatValue(args[i], locale, timeZone);
            result = result.substring(0, index) + value + result.substring(index + placeholder.length());
        }

        return result;
    }

    /**
     * 格式化命名参数（使用正则表达式，更高效）
     *
     * @param template 模板字符串
     * @param map 变量映射
     * @return 格式化后的字符串
     */
    public static String replaceNamedParameters(final String template, final Map<String, ?> map) {
        return replaceNamedParameters(template, Locale.getDefault(), TimeZone.getDefault(), map);
    }

    public static String replaceNamedParameters(final String template, final Locale locale, final Map<String, ?> map) {
        return replaceNamedParameters(template, locale, TimeZone.getDefault(), map);
    }

    /**
     * 格式化命名参数（使用正则表达式，更高效，支持本地化）
     *
     * @param template 模板字符串
     * @param map 变量映射
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    public static String replaceNamedParameters(
            final String template, final Locale locale, TimeZone timeZone, final Map<String, ?> map) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        if (map == null || map.isEmpty()) {
            return template;
        }

        Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = map.get(key);
            String replacement;
            if (value != null) {
                replacement = LocaleFormatUtils.formatValue(value, locale, timeZone);
                // 转义特殊字符
                replacement = Matcher.quoteReplacement(replacement);
            } else {
                // 如果参数不存在，保留原始占位符
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 格式化索引参数（使用正则表达式，更高效）
     *
     * @param template 模板字符串
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    public static String replaceIndexedParameters(final String template, final Object... args) {
        return replaceIndexedParameters(template, Locale.getDefault(), TimeZone.getDefault(), args);
    }

    /**
     * 格式化索引参数（使用正则表达式，更高效，支持本地化）
     *
     * @param template 模板字符串
     * @param locale 本地化设置
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    public static String replaceIndexedParameters(
            final String template, final Locale locale, TimeZone timeZone, final Object... args) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        Matcher matcher = INDEXED_PARAMETER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            try {
                int index = Integer.parseInt(matcher.group(1));
                if (index >= 0 && index < args.length) {
                    String replacement = LocaleFormatUtils.formatValue(args[index], locale, timeZone);
                    // 转义特殊字符
                    replacement = Matcher.quoteReplacement(replacement);
                    matcher.appendReplacement(result, replacement);
                } else {
                    // 索引超出范围，保持原样
                    matcher.appendReplacement(result, matcher.group(0));
                }
            } catch (NumberFormatException e) {
                // 解析索引失败，保持原样
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 智能格式化：自动检测模板类型并选择合适的格式化方法
     *
     * @param template 模板字符串
     * @param args 参数（可以是Map或Object数组）
     * @return 格式化后的字符串
     */
    public static String format(final String template, final Object args) {
        return format(template, Locale.getDefault(), TimeZone.getDefault(), args);
    }

    /**
     * 智能格式化：自动检测模板类型并选择合适的格式化方法（支持本地化）
     *
     * @param template 模板字符串
     * @param args 参数（可以是Map或Object数组）
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    public static String format(final String template, final Locale locale, TimeZone timeZone, final Object args) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        if (args == null) {
            return template;
        }

        if (args instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) args;

            // 检测是否包含命名参数
            if (NAMED_PARAMETER_PATTERN.matcher(template).find()) {
                return replaceNamedParameters(template, locale, timeZone, map);
            }

            // 检测是否包含索引参数
            if (INDEXED_PARAMETER_PATTERN.matcher(template).find()) {
                return replaceIndexedParameters(template, locale, map.values().toArray());
            }

            // 默认使用变量格式化
            return replaceNamedParameters(template, locale, timeZone, map);
        } else if (args instanceof Object[]) {
            Object[] array = (Object[]) args;

            // 检测是否包含索引参数
            if (INDEXED_PARAMETER_PATTERN.matcher(template).find()) {
                return replaceIndexedParameters(template, locale, array);
            }

            // 检测是否包含占位符
            if (template.contains(DEFAULT_PLACEHOLDER)) {
                return replacePlaceholders(template, locale, array);
            }

            // 默认使用占位符格式化
            return replacePlaceholders(template, locale, array);
        } else {
            // 单个对象，使用占位符格式化
            if (template.contains(DEFAULT_PLACEHOLDER)) {
                return replacePlaceholders(template, locale, args);
            }
            return template;
        }
    }

    /**
     * 检查模板是否包含占位符
     *
     * @param template 模板字符串
     * @return 是否包含占位符
     */
    public static boolean hasPlaceholders(final String template) {
        return hasPlaceholders(template, DEFAULT_PLACEHOLDER);
    }

    public static boolean hasPlaceholders(final String template, String placeholder) {
        return StringUtils.isNotBlank(template) && template.contains(placeholder);
    }

    /**
     * 检查模板是否包含命名参数
     *
     * @param template 模板字符串
     * @return 是否包含命名参数
     */
    public static boolean hasNamedParameters(final String template) {
        return StringUtils.isNotBlank(template)
                && NAMED_PARAMETER_PATTERN.matcher(template).find();
    }

    /**
     * 检查模板是否包含索引参数
     *
     * @param template 模板字符串
     * @return 是否包含索引参数
     */
    public static boolean hasIndexedParameters(final String template) {
        return StringUtils.isNotBlank(template)
                && INDEXED_PARAMETER_PATTERN.matcher(template).find();
    }

    public static int countPlaceholders(final String template) {
        return countPlaceholders(template, DEFAULT_PLACEHOLDER);
    }

    /**
     * 统计模板中的占位符数量
     *
     * @param template 模板字符串
     * @return 占位符数量
     */
    public static int countPlaceholders(final String template, String placeholder) {
        if (StringUtils.isBlank(template)) {
            return 0;
        }
        int count = 0;
        int index = -1;
        while ((index = template.indexOf(placeholder, index + 1)) != -1) {
            count++;
        }
        return count;
    }

    /**
     * 统计模板中的命名参数数量
     *
     * @param template 模板字符串
     * @return 命名参数数量
     */
    public static int countNamedParameters(final String template) {
        if (StringUtils.isBlank(template)) {
            return 0;
        }
        Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(template);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
