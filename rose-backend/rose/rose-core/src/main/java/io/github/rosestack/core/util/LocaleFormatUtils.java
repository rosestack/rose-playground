package io.github.rosestack.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 本地化格式化工具类
 * 使用Jackson ObjectMapper处理日期时间格式化，支持统一的时区和格式配置
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
public abstract class LocaleFormatUtils {
    private LocaleFormatUtils() {
    }

    /**
     * 使用Jackson ObjectMapper进行日期时间格式化
     * 这样可以统一使用应用配置的日期格式和时区设置
     */
    private static volatile ObjectMapper objectMapper;

    /**
     * NumberFormat缓存，避免重复创建
     */
    private static final Map<Locale, NumberFormat> NUMBER_FORMAT_CACHE = new ConcurrentHashMap<>();

    /**
     * 常用的null字符串
     */
    private static final String NULL_STRING = "null";

    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    /**
     * 获取ObjectMapper实例，如果未设置则使用JacksonUtil的默认实例
     *
     * @return ObjectMapper实例
     */
    private static ObjectMapper getObjectMapper() {
        return objectMapper != null ? objectMapper : JsonUtils.getObjectMapper();
    }

    /**
     * 格式化单个值
     *
     * @param value 要格式化的值
     * @return 格式化后的字符串
     */
    public static String formatValue(final Object value) {
        return formatValue(value, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * 格式化单个值（支持本地化）
     *
     * @param value  要格式化的值
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    public static String formatValue(final Object value, final Locale locale) {
        return formatValue(value, locale, TimeZone.getDefault());
    }

    /**
     * 格式化单个值（支持本地化和时区）
     *
     * @param value    要格式化的值
     * @param locale   本地化设置
     * @param timeZone 时区设置
     * @return 格式化后的字符串
     */
    public static String formatValue(final Object value, final Locale locale, final TimeZone timeZone) {
        if (value == null) {
            return NULL_STRING;
        }

        // 基本类型 - 快速路径
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Boolean || value instanceof Character) {
            return value.toString();
        }

        // 数字类型 - 统一处理
        if (value instanceof Number) {
            return formatNumber((Number) value, locale);
        }

        // 日期时间类型 - 使用Jackson ObjectMapper进行格式化，统一时区和格式处理
        if (isDateTimeType(value)) {
            return formatDateTimeWithJackson(value, locale, timeZone);
        }
        // 其他类型
        if (value instanceof Currency) {
            return formatCurrency((Currency) value, locale);
        }
        if (value instanceof Collection) {
            return formatCollection((Collection<?>) value, locale, timeZone);
        }
        if (value.getClass().isArray()) {
            return formatArray(value, locale, timeZone);
        }
        if (value instanceof TimeZone) {
            return formatTimeZone((TimeZone) value, locale);
        }

        // 对于其他类型，使用toString()
        return value.toString();
    }

    /**
     * 格式化数字（支持本地化）
     * 统一处理所有数字类型，包括BigDecimal和BigInteger
     *
     * @param number 数字
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    private static String formatNumber(final Number number, final Locale locale) {
        if (number == null) {
            return NULL_STRING;
        }

        try {
            NumberFormat formatter = getNumberFormat(locale);

            // 特殊处理BigDecimal的精度
            if (number instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) number;
                formatter.setMaximumFractionDigits(decimal.scale());
                formatter.setMinimumFractionDigits(0);
            }

            return formatter.format(number);
        } catch (Exception e) {
            log.warn("Failed to format number: {}, fallback to toString()", number, e);
            return number.toString();
        }
    }

    /**
     * 获取NumberFormat实例，使用缓存提高性能
     *
     * @param locale 本地化设置
     * @return NumberFormat实例
     */
    private static NumberFormat getNumberFormat(final Locale locale) {
        return NUMBER_FORMAT_CACHE.computeIfAbsent(locale, NumberFormat::getInstance);
    }

    /**
     * 判断是否为日期时间类型
     * 使用更高效的类型判断方式
     *
     * @param value 要判断的值
     * @return 是否为日期时间类型
     */
    private static boolean isDateTimeType(Object value) {
        Class<?> clazz = value.getClass();
        return Date.class.isAssignableFrom(clazz)
                || LocalDate.class.equals(clazz)
                || LocalTime.class.equals(clazz)
                || LocalDateTime.class.equals(clazz)
                || ZonedDateTime.class.equals(clazz)
                || Instant.class.equals(clazz);
    }

    /**
     * 使用Jackson ObjectMapper格式化日期时间类型
     * 这样可以统一使用应用配置的日期格式和时区设置，不需要关心具体的格式化样式
     *
     * @param value    日期时间对象
     * @param locale   本地化设置（注意：Jackson主要通过配置文件控制格式，locale在这里影响有限）
     * @param timeZone 时区设置，用于日期时间的时区转换
     * @return 格式化后的字符串
     */
    private static String formatDateTimeWithJackson(final Object value, final Locale locale, final TimeZone timeZone) {
        if (value == null) {
            return NULL_STRING;
        }

        try {
            // 使用Jackson进行序列化，会自动应用配置的日期格式和时区
            ObjectMapper mapper = getObjectMapper().copy().setLocale(locale).setTimeZone(timeZone);
            String jsonValue = mapper.writeValueAsString(value);

            // 移除JSON字符串的引号
            if (jsonValue.length() >= 2 && jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) {
                return jsonValue.substring(1, jsonValue.length() - 1);
            }
            return jsonValue;
        } catch (JsonProcessingException e) {
            log.warn("Failed to format datetime with Jackson: {}, fallback to toString()", value, e);
            return value.toString();
        }
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
            return NULL_STRING;
        }

        try {
            return currency.getSymbol(locale);
        } catch (Exception e) {
            log.warn("Failed to format currency: {}, fallback to currency code", currency, e);
            return currency.getCurrencyCode();
        }
    }

    /**
     * 格式化Collection（支持本地化和时区）
     * 优化：使用Stream API提高性能和可读性
     *
     * @param collection Collection
     * @param locale     本地化设置
     * @param timeZone   时区设置
     * @return 格式化后的字符串
     */
    private static String formatCollection(final Collection<?> collection, final Locale locale, final TimeZone timeZone) {
        if (collection == null) {
            return NULL_STRING;
        }

        if (collection.isEmpty()) {
            return "[]";
        }

        String elements = collection.stream()
                .map(element -> formatValue(element, locale, timeZone))
                .collect(Collectors.joining(", "));

        return "[" + elements + "]";
    }

    /**
     * 格式化Array（支持本地化）
     * 优化：简化数组处理逻辑，使用更高效的方式
     *
     * @param array  数组
     * @param locale 本地化设置
     * @return 格式化后的字符串
     */
    private static String formatArray(final Object array, final Locale locale, final TimeZone timeZone) {
        if (array == null) {
            return NULL_STRING;
        }

        // 对象数组
        if (array instanceof Object[]) {
            return formatCollection(Arrays.asList((Object[]) array), locale, timeZone);
        }

        // 基本类型数组 - 使用更简洁的方式
        if (array instanceof int[]) {
            return formatCollection(Arrays.stream((int[]) array).boxed().collect(Collectors.toList()), locale, timeZone);
        }
        if (array instanceof long[]) {
            return formatCollection(Arrays.stream((long[]) array).boxed().collect(Collectors.toList()), locale, timeZone);
        }
        if (array instanceof double[]) {
            return formatCollection(Arrays.stream((double[]) array).boxed().collect(Collectors.toList()), locale, timeZone);
        }
        if (array instanceof float[]) {
            float[] floats = (float[]) array;
            List<Float> list = new ArrayList<>(floats.length);
            for (float f : floats) {
                list.add(f);
            }
            return formatCollection(list, locale, timeZone);
        }
        if (array instanceof boolean[]) {
            boolean[] booleans = (boolean[]) array;
            List<Boolean> list = new ArrayList<>(booleans.length);
            for (boolean b : booleans) {
                list.add(b);
            }
            return formatCollection(list, locale, timeZone);
        }
        if (array instanceof char[]) {
            char[] chars = (char[]) array;
            List<Character> list = new ArrayList<>(chars.length);
            for (char c : chars) {
                list.add(c);
            }
            return formatCollection(list, locale, timeZone);
        }
        if (array instanceof byte[]) {
            byte[] bytes = (byte[]) array;
            List<Byte> list = new ArrayList<>(bytes.length);
            for (byte b : bytes) {
                list.add(b);
            }
            return formatCollection(list, locale, timeZone);
        }
        if (array instanceof short[]) {
            short[] shorts = (short[]) array;
            List<Short> list = new ArrayList<>(shorts.length);
            for (short s : shorts) {
                list.add(s);
            }
            return formatCollection(list, locale, timeZone);
        }

        return array.toString();
    }

    /**
     * 格式化TimeZone（支持本地化）
     * 改进：添加异常处理
     *
     * @param timeZone TimeZone
     * @param locale   本地化设置
     * @return 格式化后的字符串
     */
    private static String formatTimeZone(final TimeZone timeZone, final Locale locale) {
        if (timeZone == null) {
            return NULL_STRING;
        }

        try {
            return timeZone.getDisplayName(locale);
        } catch (Exception e) {
            log.warn("Failed to format timezone: {}, fallback to ID", timeZone, e);
            return timeZone.getID();
        }
    }

    /**
     * 清理缓存，主要用于测试或内存管理
     */
    public static void clearCache() {
        NUMBER_FORMAT_CACHE.clear();
    }
}
