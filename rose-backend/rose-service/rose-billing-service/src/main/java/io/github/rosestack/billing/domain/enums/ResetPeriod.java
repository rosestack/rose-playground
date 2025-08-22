package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 重置周期枚举
 * <p>
 * 定义功能配额和使用量的重置频率
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ResetPeriod {

	/**
	 * 每日重置 - 每天重置配额和使用量
	 * 适用场景：日度API调用限制、日度消息发送等
	 */
	DAY("每日重置"),

	/**
	 * 每月重置 - 每月重置配额和使用量
	 * 适用场景：月度流量、月度API调用、月度存储等
	 */
	MONTH("每月重置"),

	/**
	 * 每年重置 - 每年重置配额和使用量
	 * 适用场景：年度报告生成、年度数据导出等
	 */
	YEAR("每年重置"),

	/**
	 * 永不重置 - 配额不会自动重置
	 * 适用场景：用户数量、项目数量、永久存储空间等
	 */
	NEVER("永不重置");

	private final String description;
}
