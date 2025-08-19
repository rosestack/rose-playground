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

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * 时长格式化器，用于格式化输出两个日期相差的时长<br>
 * 根据{@link Level}不同，调用{@link #format()}方法后返回类似于：
 *
 * <ul>
 *   <li>XX小时XX分XX秒
 *   <li>XX天XX小时
 *   <li>XX月XX天XX小时
 * </ul>
 */
public class BetweenFormatter implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 格式化级别的最大个数
	 */
	private final int levelMaxCount;

	/**
	 * 时长毫秒数
	 */
	private long betweenMs;

	/**
	 * 格式化级别
	 */
	private Level level;

	/**
	 * 构造
	 *
	 * @param betweenMs 日期间隔
	 * @param level     级别，按照天、小时、分、秒、毫秒分为5个等级，根据传入等级，格式化到相应级别
	 */
	public BetweenFormatter(long betweenMs, Level level) {
		this(betweenMs, level, 0);
	}

	/**
	 * 构造
	 *
	 * @param betweenMs     日期间隔
	 * @param level         级别，按照天、小时、分、秒、毫秒分为5个等级，根据传入等级，格式化到相应级别
	 * @param levelMaxCount 格式化级别的最大个数，假如级别个数为1，但是级别到秒，那只显示一个级别
	 */
	public BetweenFormatter(long betweenMs, Level level, int levelMaxCount) {
		this.betweenMs = betweenMs;
		this.level = level;
		this.levelMaxCount = levelMaxCount;
	}

	/**
	 * 格式化日期间隔输出<br>
	 *
	 * @return 格式化后的字符串
	 */
	public String format() {
		final StringBuilder sb = new StringBuilder();
		if (betweenMs <= 0) {
			sb.append(0).append(this.level.name);
			return sb.toString();
		}

		long day = betweenMs / DateUnit.DAY.getMillis();
		long hour = betweenMs / DateUnit.HOUR.getMillis() - day * 24;
		long minute = betweenMs / DateUnit.MINUTE.getMillis() - day * 24 * 60 - hour * 60;

		final long BetweenOfSecond = ((day * 24 + hour) * 60 + minute) * 60;
		long second = betweenMs / DateUnit.SECOND.getMillis() - BetweenOfSecond;
		long millisecond = betweenMs - (BetweenOfSecond + second) * 1000;

		final int level = this.level.ordinal();
		int levelCount = 0;

		if (isLevelCountValid(levelCount) && 0 != day && level >= Level.DAY.ordinal()) {
			sb.append(day).append(Level.DAY.name);
			levelCount++;
		}
		if (isLevelCountValid(levelCount) && 0 != hour && level >= Level.HOUR.ordinal()) {
			sb.append(hour).append(Level.HOUR.name);
			levelCount++;
		}
		if (isLevelCountValid(levelCount) && 0 != minute && level >= Level.MINUTE.ordinal()) {
			sb.append(minute).append(Level.MINUTE.name);
			levelCount++;
		}
		if (isLevelCountValid(levelCount) && 0 != second && level >= Level.SECOND.ordinal()) {
			sb.append(second).append(Level.SECOND.name);
			levelCount++;
		}
		if (isLevelCountValid(levelCount) && 0 != millisecond && level >= Level.MILLISECOND.ordinal()) {
			sb.append(millisecond).append(Level.MILLISECOND.name);
			// levelCount++;
		}

		return sb.toString();
	}

	/**
	 * 获得 时长毫秒数
	 *
	 * @return 时长毫秒数
	 */
	public long getBetweenMs() {
		return betweenMs;
	}

	/**
	 * 设置 时长毫秒数
	 *
	 * @param betweenMs 时长毫秒数
	 */
	public void setBetweenMs(long betweenMs) {
		this.betweenMs = betweenMs;
	}

	/**
	 * 获得 格式化级别
	 *
	 * @return 格式化级别
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * 设置格式化级别
	 *
	 * @param level 格式化级别
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return format();
	}

	/**
	 * 等级数量是否有效<br>
	 * 有效的定义是：levelMaxCount大于0（被设置），当前等级数量没有超过这个最大值
	 *
	 * @param levelCount 登记数量
	 * @return 是否有效
	 */
	private boolean isLevelCountValid(int levelCount) {
		return this.levelMaxCount <= 0 || levelCount < this.levelMaxCount;
	}

	/**
	 * 格式化等级枚举
	 *
	 * @author Looly
	 */
	public enum Level {

		/**
		 * 天
		 */
		DAY("天"),
		/**
		 * 小时
		 */
		HOUR("小时"),
		/**
		 * 分钟
		 */
		MINUTE("分钟"),
		/**
		 * 秒
		 */
		SECOND("秒"),
		/**
		 * 毫秒
		 */
		MILLISECOND("毫秒");

		/**
		 * 级别名称
		 */
		private final String name;

		/**
		 * 构造
		 *
		 * @param name 级别名称
		 */
		Level(String name) {
			this.name = name;
		}

		/**
		 * 获取级别名称
		 *
		 * @return 级别名称
		 */
		public String getName() {
			return this.name;
		}
	}

	public enum DateUnit {

		/**
		 * 一毫秒
		 */
		MS(1),
		/**
		 * 一秒的毫秒数
		 */
		SECOND(1000),
		/**
		 * 一分钟的毫秒数
		 */
		MINUTE(SECOND.getMillis() * 60),
		/**
		 * 一小时的毫秒数
		 */
		HOUR(MINUTE.getMillis() * 60),
		/**
		 * 一天的毫秒数
		 */
		DAY(HOUR.getMillis() * 24),
		/**
		 * 一周的毫秒数
		 */
		WEEK(DAY.getMillis() * 7);

		/**
		 * -- GETTER --
		 *
		 * @return 单位对应的毫秒数
		 */
		private final long millis;

		DateUnit(long millis) {
			this.millis = millis;
		}

		/**
		 * 单位兼容转换，将{@link ChronoUnit}转换为对应的DateUnit
		 *
		 * @param unit {@link ChronoUnit}
		 * @return DateUnit，null表示不支持此单位
		 * @since 5.4.5
		 */
		public static DateUnit of(ChronoUnit unit) {
			switch (unit) {
				case MICROS:
					return DateUnit.MS;
				case SECONDS:
					return DateUnit.SECOND;
				case MINUTES:
					return DateUnit.MINUTE;
				case HOURS:
					return DateUnit.HOUR;
				case DAYS:
					return DateUnit.DAY;
				case WEEKS:
					return DateUnit.WEEK;
			}
			return null;
		}

		/**
		 * 单位兼容转换，将DateUnit转换为对应的{@link ChronoUnit}
		 *
		 * @param unit DateUnit
		 * @return {@link ChronoUnit}
		 * @since 5.4.5
		 */
		public static ChronoUnit toChronoUnit(DateUnit unit) {
			switch (unit) {
				case MS:
					return ChronoUnit.MICROS;
				case SECOND:
					return ChronoUnit.SECONDS;
				case MINUTE:
					return ChronoUnit.MINUTES;
				case HOUR:
					return ChronoUnit.HOURS;
				case DAY:
					return ChronoUnit.DAYS;
				case WEEK:
					return ChronoUnit.WEEKS;
			}
			return null;
		}

		public long getMillis() {
			return millis;
		}

		/**
		 * 单位兼容转换，将DateUnit转换为对应的{@link ChronoUnit}
		 *
		 * @return {@link ChronoUnit}
		 * @since 5.4.5
		 */
		public ChronoUnit toChronoUnit() {
			return DateUnit.toChronoUnit(this);
		}
	}
}
