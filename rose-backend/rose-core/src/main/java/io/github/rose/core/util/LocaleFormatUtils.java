package io.github.rose.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class LocaleFormatUtils {
    // 简化的日期时间格式化 - 建议使用专门的框架处理复杂格式化
    private static final java.time.format.DateTimeFormatter DEFAULT_DATE_FORMATTER =
            java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM);
    private static final java.time.format.DateTimeFormatter DEFAULT_TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.MEDIUM);
    private static final java.time.format.DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM);

    /**
     * 格式化单个值
     *
     * @param value 要格式化的值
     * @return 格式化后的字符串
     */
    public static String formatValue(final Object value) {
        return formatValue(value, Locale.getDefault());
    }

    /**
     * 格式化单个值（支持本地化）
     *
     * @param value  要格式化的值
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    public static String formatValue(final Object value, final Locale locale) {
        if (value == null) {
            return "null";
        }

        // 基本类型
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Character) {
            return value.toString();
        }

        // 数字类型
        if (value instanceof Number) {
            return formatNumber((Number) value, locale);
        }
        if (value instanceof BigDecimal) {
            return formatBigDecimal((BigDecimal) value, locale);
        }
        if (value instanceof BigInteger) {
            return formatBigInteger((BigInteger) value, locale);
        }

        // 日期时间类型 - 使用简化的格式化，建议使用专门的框架处理复杂格式化
        if (value instanceof Date) {
            return formatDate((Date) value, locale);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DEFAULT_DATE_FORMATTER.withLocale(locale));
        }
        if (value instanceof LocalTime) {
            return ((LocalTime) value).format(DEFAULT_TIME_FORMATTER.withLocale(locale));
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DEFAULT_DATETIME_FORMATTER.withLocale(locale));
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).format(DEFAULT_DATETIME_FORMATTER.withLocale(locale));
        }
        if (value instanceof Instant) {
            LocalDateTime dateTime = LocalDateTime.ofInstant((Instant) value, ZoneId.systemDefault());
            return dateTime.format(DEFAULT_DATETIME_FORMATTER.withLocale(locale));
        }
        if (value instanceof Duration) {
            return formatDuration((Duration) value, locale);
        }
        if (value instanceof Period) {
            return formatPeriod((Period) value, locale);
        }

        // 其他类型
        if (value instanceof Currency) {
            return formatCurrency((Currency) value, locale);
        }
        if (value instanceof Enum) {
            return formatEnum((Enum<?>) value, locale);
        }
        if (value instanceof Collection) {
            return formatCollection((Collection<?>) value, locale);
        }
        if (value.getClass().isArray()) {
            return formatArray(value, locale);
        }
        if (value instanceof TimeZone) {
            return formatTimeZone((TimeZone) value, locale);
        }

        // 对于其他类型，使用toString()
        return value.toString();
    }

    /**
     * 格式化数字（支持本地化）
     *
     * @param number 数字
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    private static String formatNumber(final Number number, final Locale locale) {
        if (number == null) {
            return "null";
        }

        NumberFormat formatter = NumberFormat.getInstance(locale);
        return formatter.format(number);
    }

    /**
     * 格式化BigDecimal（支持本地化）
     *
     * @param decimal BigDecimal
     * @param locale  本地化设置
     * @return 格式化后的字符串
     */
    private static String formatBigDecimal(final BigDecimal decimal, final Locale locale) {
        if (decimal == null) {
            return "null";
        }

        NumberFormat formatter = NumberFormat.getInstance(locale);
        formatter.setMaximumFractionDigits(decimal.scale());
        return formatter.format(decimal);
    }

    /**
     * 格式化BigInteger（支持本地化）
     *
     * @param integer BigInteger
     * @param locale  本地化设置
     * @return 格式化后的字符串
     */
    private static String formatBigInteger(final BigInteger integer, final Locale locale) {
        if (integer == null) {
            return "null";
        }

        NumberFormat formatter = NumberFormat.getInstance(locale);
        return formatter.format(integer);
    }

    /**
     * 格式化Date（支持本地化）
     *
     * @param date   Date对象
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    private static String formatDate(final Date date, final Locale locale) {
        if (date == null) {
            return "null";
        }
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        return formatter.format(date);
    }

    /**
     * 格式化Duration（支持本地化）
     *
     * @param duration Duration对象
     * @param locale   本地化设置
     * @return 格式化后的字符串
     */
    private static String formatDuration(final Duration duration, final Locale locale) {
        if (duration == null) {
            return "null";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format(locale, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(locale, "%d:%02d", minutes, seconds);
        }
    }

    /**
     * 格式化Period（支持本地化）
     *
     * @param period Period对象
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    private static String formatPeriod(final Period period, final Locale locale) {
        if (period == null) {
            return "null";
        }
        List<String> parts = new ArrayList<>();
        if (period.getYears() > 0) {
            parts.add(period.getYears() + "年");
        }
        if (period.getMonths() > 0) {
            parts.add(period.getMonths() + "月");
        }
        if (period.getDays() > 0) {
            parts.add(period.getDays() + "天");
        }

        return parts.isEmpty() ? "0天" : String.join("", parts);
    }

    /**
     * 格式化Currency（支持本地化）
     *
     * @param currency Currency
     * @param locale   本地化设置
     * @return 格式化后的字符串
     */
    private static String formatCurrency(final Currency currency, final Locale locale) {
        if (currency == null) {
            return "null";
        }

        return currency.getSymbol(locale);
    }

    /**
     * 格式化Enum（支持本地化）
     *
     * @param enumValue Enum
     * @param locale    本地化设置
     * @return 格式化后的字符串
     */
    private static String formatEnum(final Enum<?> enumValue, final Locale locale) {
        if (enumValue == null) {
            return "null";
        }

        // 可以根据需要实现更复杂的枚举本地化逻辑
        return enumValue.name();
    }

    /**
     * 格式化Collection（支持本地化）
     *
     * @param collection Collection
     * @param locale     本地化设置
     * @return 格式化后的字符串
     */
    private static String formatCollection(final Collection<?> collection, final Locale locale) {
        if (collection == null) {
            return "null";
        }

        List<String> formattedElements = new ArrayList<>();
        for (Object element : collection) {
            formattedElements.add(formatValue(element, locale));
        }

        return "[" + String.join(", ", formattedElements) + "]";
    }

    /**
     * 格式化Array（支持本地化）
     *
     * @param array  数组
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    private static String formatArray(final Object array, final Locale locale) {
        if (array == null) {
            return "null";
        }

        if (array instanceof Object[]) {
            return formatCollection(Arrays.asList((Object[]) array), locale);
        } else if (array instanceof int[]) {
            return formatCollection(Arrays.stream((int[]) array).boxed().toList(), locale);
        } else if (array instanceof long[]) {
            return formatCollection(Arrays.stream((long[]) array).boxed().toList(), locale);
        } else if (array instanceof double[]) {
            return formatCollection(Arrays.stream((double[]) array).boxed().toList(), locale);
        } else if (array instanceof float[]) {
            List<Float> floatList = new ArrayList<>();
            for (float f : (float[]) array) {
                floatList.add(f);
            }
            return formatCollection(floatList, locale);
        } else if (array instanceof boolean[]) {
            List<Boolean> booleanList = new ArrayList<>();
            for (boolean b : (boolean[]) array) {
                booleanList.add(b);
            }
            return formatCollection(booleanList, locale);
        } else if (array instanceof char[]) {
            List<Character> charList = new ArrayList<>();
            for (char c : (char[]) array) {
                charList.add(c);
            }
            return formatCollection(charList, locale);
        } else if (array instanceof byte[]) {
            List<Byte> byteList = new ArrayList<>();
            for (byte b : (byte[]) array) {
                byteList.add(b);
            }
            return formatCollection(byteList, locale);
        } else if (array instanceof short[]) {
            List<Short> shortList = new ArrayList<>();
            for (short s : (short[]) array) {
                shortList.add(s);
            }
            return formatCollection(shortList, locale);
        }

        return array.toString();
    }

    /**
     * 格式化TimeZone（支持本地化）
     *
     * @param timeZone TimeZone
     * @param locale   本地化设置
     * @return 格式化后的字符串
     */
    private static String formatTimeZone(final TimeZone timeZone, final Locale locale) {
        if (timeZone == null) {
            return "null";
        }

        return timeZone.getDisplayName(locale);
    }
}
