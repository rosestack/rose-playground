package io.github.rose.core.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

/**
 * 统一的格式化工具类
 * <p>
 * 提供模板格式化、变量替换和值格式化功能
 *
 * @author rose
 * @since 0.0.1
 */
@Slf4j
public abstract class FormatUtils {
    public static final String DEFAULT_PLACEHOLDER = "{}";

    /**
     * 格式化模板变量
     *
     * @param template 模板字符串
     * @param map      变量映射
     * @return 格式化后的字符串
     */
    public static String formatVariables(final String template, final Map<String, ?> map) {
        return formatVariables(template, "{", "}", map);
    }

    /**
     * 格式化模板变量（自定义分隔符）
     *
     * @param template 模板字符串
     * @param prefix   前缀
     * @param suffix   后缀
     * @param map      变量映射
     * @return 格式化后的字符串
     */
    public static String formatVariables(
            final String template, String prefix, String suffix, final Map<String, ?> map) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template;
        }

        String template2 = template;
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
            try {
                String key = entry.getKey();
                Object value = entry.getValue();
                String replacement = value == null ? "null" : value.toString();
                template2 = template2.replace(prefix + key + suffix, replacement);
            } catch (Exception e) {
                log.error("Failed to format template: " + template, e);
            }
        }
        return template2;
    }

    /**
     * 格式化占位符
     *
     * @param template 模板字符串
     * @param args     参数数组
     * @return 格式化后的字符串
     */
    public static String format(final String template, final Object... args) {
        return formatPlaceholder(template, DEFAULT_PLACEHOLDER, args);
    }

    /**
     * 格式化占位符（自定义占位符）
     *
     * @param template    模板字符串
     * @param placeholder 占位符
     * @param args        参数数组
     * @return 格式化后的字符串
     */
    public static String formatPlaceholder(final String template, final String placeholder, final Object... args) {
        int argsLength = args == null ? 0 : args.length;
        if (argsLength == 0) {
            return template;
        }

        StringBuilder stringBuilder = new StringBuilder(template);
        int index = -1;
        for (int i = 0; i < argsLength; i++) {
            index = stringBuilder.indexOf(placeholder);
            if (index == -1) {
                break;
            }
            String value = String.valueOf(args[i]);
            stringBuilder.replace(index, index + placeholder.length(), value);
        }
        return stringBuilder.toString();
    }
}