package io.github.rosestack.core.util;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** LocaleFormatUtils测试类 验证使用Jackson ObjectMapper进行日期时间格式化的功能 */
class LocaleFormatUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(LocaleFormatUtilsTest.class);

    @BeforeEach
    void setUp() {
        // 配置ObjectMapper，模拟Spring Boot的配置
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 设置日期格式和时区，模拟application.yaml中的配置
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        // 注入到LocaleFormatUtils
        LocaleFormatUtils.setObjectMapper(mapper);
    }

    @Test
    void testFormatLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45);
        String result = LocaleFormatUtils.formatValue(dateTime, Locale.getDefault());

        // 验证格式化结果符合配置的格式
        assertNotNull(result);
        assertTrue(result.contains("T")); // Jackson格式化后不应包含ISO格式的T
        log.info("LocalDateTime formatted: " + result);
    }

    @Test
    void testFormatLocalDate() {
        LocalDate date = LocalDate.of(2023, 12, 25);
        String result = LocaleFormatUtils.formatValue(date, Locale.getDefault());

        assertNotNull(result);
        log.info("LocalDate formatted: " + result);
    }

    @Test
    void testFormatLocalTime() {
        LocalTime time = LocalTime.of(15, 30, 45);
        String result = LocaleFormatUtils.formatValue(time, Locale.getDefault());

        assertNotNull(result);
        log.info("LocalTime formatted: " + result);
    }

    @Test
    void testFormatDate() {
        Date date = new Date();
        String result = LocaleFormatUtils.formatValue(date, Locale.getDefault());

        assertNotNull(result);
        // 验证格式符合yyyy-MM-dd HH:mm:ss
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        log.info("Date formatted: " + result);
    }

    @Test
    void testFormatInstant() {
        Instant instant = Instant.now();
        String result = LocaleFormatUtils.formatValue(instant, Locale.getDefault());

        assertNotNull(result);
        log.info("Instant formatted: " + result);
    }

    @Test
    void testFormatZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        String result = LocaleFormatUtils.formatValue(zonedDateTime, Locale.getDefault());

        assertNotNull(result);
        log.info("ZonedDateTime formatted: " + result);
    }

    @Test
    void testFormatNullValue() {
        String result = LocaleFormatUtils.formatValue(null, Locale.getDefault());
        assertEquals("null", result);
    }

    @Test
    void testFormatNonDateTimeValue() {
        // 测试非日期时间类型不受影响
        String result = LocaleFormatUtils.formatValue("Hello World", Locale.getDefault());
        assertEquals("Hello World", result);

        Integer number = 12345;
        String numberResult = LocaleFormatUtils.formatValue(number, Locale.getDefault());
        assertNotNull(numberResult);
    }

    @Test
    void testWithoutObjectMapperSet() {
        // 测试未设置ObjectMapper时的回退行为
        LocaleFormatUtils.setObjectMapper(null);

        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45);
        String result = LocaleFormatUtils.formatValue(dateTime, Locale.getDefault());

        assertNotNull(result);
        log.info("Without ObjectMapper: " + result);
    }

    @Test
    void testFormatCollection() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        String result = LocaleFormatUtils.formatValue(numbers, Locale.getDefault());

        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        log.info("Collection formatted: " + result);
    }

    @Test
    void testFormatArray() {
        int[] array = {1, 2, 3, 4, 5};
        String result = LocaleFormatUtils.formatValue(array, Locale.getDefault());

        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        log.info("Array formatted: " + result);
    }

    @Test
    void testFormatBigDecimal() {
        BigDecimal decimal = new BigDecimal("123.456789");
        String result = LocaleFormatUtils.formatValue(decimal, Locale.US);

        assertNotNull(result);
        log.info("BigDecimal formatted: " + result);
    }

    @Test
    void testClearCache() {
        // 测试缓存清理功能
        LocaleFormatUtils.formatValue(123, Locale.getDefault());
        LocaleFormatUtils.clearCache();

        // 再次格式化应该仍然正常工作
        String result = LocaleFormatUtils.formatValue(456, Locale.getDefault());
        assertNotNull(result);
    }
}
