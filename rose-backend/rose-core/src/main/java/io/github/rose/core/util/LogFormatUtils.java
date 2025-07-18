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
public abstract class LogFormatUtils {
    /**
     * 格式化值（用于日志输出）
     *
     * @param value  要格式化的值
     * @param locale 语言环境
     * @return 格式化后的字符串
     */
    public static String formatValueForLog(Object value, Locale locale) {
        if (value == null) {
            return "null";
        }

        try {
            // 字符串类型
            if (value instanceof String) {
                return (String) value;
            }

            // 数字类型
            if (value instanceof Number) {
                return formatNumberForLog((Number) value, locale);
            }

            // 日期时间类型
            if (value instanceof java.util.Date) {
                return formatDateForLog((java.util.Date) value, locale);
            }

            if (value instanceof java.time.LocalDateTime) {
                return formatLocalDateTimeForLog((java.time.LocalDateTime) value, locale);
            }

            if (value instanceof java.time.LocalDate) {
                return formatLocalDateForLog((java.time.LocalDate) value, locale);
            }

            if (value instanceof java.time.LocalTime) {
                return formatLocalTimeForLog((java.time.LocalTime) value, locale);
            }

            if (value instanceof java.time.ZonedDateTime) {
                return formatZonedDateTimeForLog((java.time.ZonedDateTime) value, locale);
            }

            // 布尔类型
            if (value instanceof Boolean) {
                return value.toString();
            }

            // 字符类型
            if (value instanceof Character) {
                return value.toString();
            }

            // 数组类型
            if (value.getClass().isArray()) {
                return formatArrayForLog(value);
            }

            // 集合类型
            if (value instanceof java.util.Collection) {
                return formatCollectionForLog((java.util.Collection<?>) value);
            }

            // Map类型
            if (value instanceof Map) {
                return formatMapForLog((Map<?, ?>) value);
            }

            // 其他对象类型
            return value.toString();

        } catch (Exception e) {
            return value.toString();
        }
    }

    /**
     * 格式化值（使用默认语言环境）
     *
     * @param value 要格式化的值
     * @return 格式化后的字符串
     */
    public static String formatValueForLog(Object value) {
        return formatValueForLog(value, Locale.getDefault());
    }

    /**
     * 安全格式化值（不会抛出异常）
     *
     * @param value  要格式化的值
     * @param locale 语言环境
     * @return 格式化后的字符串，如果格式化失败则返回原始值的字符串表示
     */
    public static String safeFormatValueForLog(Object value, Locale locale) {
        try {
            return formatValueForLog(value, locale);
        } catch (Exception e) {
            return value != null ? value.toString() : "null";
        }
    }

    /**
     * 安全格式化值（使用默认语言环境）
     *
     * @param value 要格式化的值
     * @return 格式化后的字符串
     */
    public static String safeFormatValueForLog(Object value) {
        return safeFormatValueForLog(value, Locale.getDefault());
    }

    // ==================== 私有辅助方法 ====================

    private static String formatNumberForLog(Number number, Locale locale) {
        try {
            NumberFormat nf = NumberFormat.getInstance(locale);

            if (number instanceof Integer || number instanceof Long) {
                return nf.format(number.longValue());
            } else if (number instanceof Float || number instanceof Double) {
                return nf.format(number.doubleValue());
            } else {
                return nf.format(number);
            }
        } catch (Exception e) {
            return number.toString();
        }
    }

    private static String formatDateForLog(java.util.Date date, Locale locale) {
        try {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
            return df.format(date);
        } catch (Exception e) {
            return date.toString();
        }
    }

    private static String formatLocalDateTimeForLog(java.time.LocalDateTime dateTime, Locale locale) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateTime.toString();
        }
    }

    private static String formatLocalDateForLog(java.time.LocalDate date, Locale locale) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            return date.format(formatter);
        } catch (Exception e) {
            return date.toString();
        }
    }

    private static String formatLocalTimeForLog(java.time.LocalTime time, Locale locale) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale);
            return time.format(formatter);
        } catch (Exception e) {
            return time.toString();
        }
    }

    private static String formatZonedDateTimeForLog(java.time.ZonedDateTime dateTime, Locale locale) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateTime.toString();
        }
    }

    private static String formatArrayForLog(Object array) {
        try {
            if (array instanceof Object[]) {
                Object[] objArray = (Object[]) array;
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < objArray.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(formatValueForLog(objArray[i], Locale.getDefault()));
                }
                sb.append("]");
                return sb.toString();
            } else if (array instanceof int[]) {
                return java.util.Arrays.toString((int[]) array);
            } else if (array instanceof long[]) {
                return java.util.Arrays.toString((long[]) array);
            } else if (array instanceof double[]) {
                return java.util.Arrays.toString((double[]) array);
            } else if (array instanceof float[]) {
                return java.util.Arrays.toString((float[]) array);
            } else if (array instanceof boolean[]) {
                return java.util.Arrays.toString((boolean[]) array);
            } else if (array instanceof char[]) {
                return java.util.Arrays.toString((char[]) array);
            } else if (array instanceof byte[]) {
                return java.util.Arrays.toString((byte[]) array);
            } else if (array instanceof short[]) {
                return java.util.Arrays.toString((short[]) array);
            } else {
                return array.toString();
            }
        } catch (Exception e) {
            return array.toString();
        }
    }

    private static String formatCollectionForLog(java.util.Collection<?> collection) {
        try {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(formatValueForLog(item, Locale.getDefault()));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return collection.toString();
        }
    }

    private static String formatMapForLog(Map<?, ?> map) {
        try {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(formatValueForLog(entry.getKey(), Locale.getDefault()))
                        .append("=")
                        .append(formatValueForLog(entry.getValue(), Locale.getDefault()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return map.toString();
        }
    }
}