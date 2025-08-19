/*
 * Copyright © 2025 rosestack.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.rosestack.core.util.date;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 日期格式
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 0.0.1
 */
public class DatePattern {
	public static final String NORM_MONTH = "yyyy-MM";
	public static final DateTimeFormatter NORM_MONTH_FORMATTER = createFormatter(NORM_MONTH);

	public static final String SIMPLE_MONTH = "yyyyMM";
	public static final DateTimeFormatter SIMPLE_MONTH_FORMATTER = createFormatter(SIMPLE_MONTH);

	public static final String NORM_DATE = "yyyy-MM-dd";
	public static final DateTimeFormatter NORM_DATE_FORMATTER = createFormatter(NORM_DATE);

	public static final String NORM_TIME = "HH:mm:ss";
	public static final DateTimeFormatter NORM_TIME_FORMATTER = createFormatter(NORM_TIME);

	public static final String NORM_DATETIME_MINUTE = "yyyy-MM-dd HH:mm";
	public static final DateTimeFormatter NORM_DATETIME_MINUTE_FORMATTER = createFormatter(NORM_DATETIME_MINUTE);

	public static final String NORM_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final DateTimeFormatter NORM_DATETIME_FORMATTER = createFormatter(NORM_DATETIME);

	public static final String NORM_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final DateTimeFormatter NORM_DATETIME_MS_FORMATTER = createFormatter(NORM_DATETIME_MS);

	public static final String ISO8601 = "yyyy-MM-dd HH:mm:ss,SSS";
	public static final DateTimeFormatter ISO8601_FORMATTER = createFormatter(ISO8601);

	public static final String CHINESE_DATE = "yyyy年MM月dd日";
	public static final DateTimeFormatter CHINESE_DATE_FORMATTER = createFormatter(CHINESE_DATE);

	public static final String CHINESE_DATE_TIME = "yyyy年MM月dd日HH时mm分ss秒";
	public static final DateTimeFormatter CHINESE_DATE_TIME_FORMATTER = createFormatter(CHINESE_DATE_TIME);

	public static final String PURE_DATE = "yyyyMMdd";
	public static final DateTimeFormatter PURE_DATE_FORMATTER = createFormatter(PURE_DATE);

	public static final String PURE_TIME = "HHmmss";
	public static final DateTimeFormatter PURE_TIME_FORMATTER = createFormatter(PURE_TIME);

	public static final String PURE_DATETIME = "yyyyMMddHHmmss";
	public static final DateTimeFormatter PURE_DATETIME_FORMATTER = createFormatter(PURE_DATETIME);

	public static final String PURE_DATETIME_MS = "yyyyMMddHHmmssSSS";
	public static final DateTimeFormatter PURE_DATETIME_MS_FORMATTER = createFormatter(PURE_DATETIME_MS);

	public static final String HTTP_DATETIME = "EEE, dd MMM yyyy HH:mm:ss z";
	public static final String JDK_DATETIME = "EEE MMM dd HH:mm:ss zzz yyyy";
	public static final String UTC_SIMPLE = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String UTC_SIMPLE_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String UTC_WITH_ZONE_OFFSET = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String UTC_WITH_XXX_OFFSET = "yyyy-MM-dd'T'HH:mm:ssXXX";
	public static final String UTC_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final String UTC_MS_WITH_ZONE_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String UTC_MS_WITH_XXX_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	public static DateTimeFormatter createFormatter(String pattern) {
		return createFormatter(pattern, Locale.getDefault(), ZoneId.systemDefault());
	}

	public static DateTimeFormatter createFormatter(String pattern, Locale locale, ZoneId zoneId) {
		return DateTimeFormatter.ofPattern(pattern, locale).withZone(zoneId);
	}
}
