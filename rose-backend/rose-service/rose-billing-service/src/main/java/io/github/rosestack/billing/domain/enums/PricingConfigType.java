package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 定价配置类型枚举
 * <p>
 * 定义系统中支持的定价配置类型，用于pricing_config中的type字段
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PricingConfigType {

	/**
	 * 配额定价计算
	 * 特点：超出配额的部分需要额外付费
	 * 适用场景：套餐包含免费额度的功能
	 * 示例配置：{"type": "quota", "values": [{"min": 0, "max": 1000, "price": 0}, {"min": 1001, "max": null, "price": 0.001}]}
	 * 含义：0-1000次免费，1001次以上每次0.001元
	 */
	QUOTA("配额定价"),

	/**
	 * 阶梯定价计算
	 * 特点：每个阶梯内按对应单价计费，使用量越大单价越低
	 * 适用场景：使用量越大单价越低的优惠策略
	 * 示例配置：{"type": "tiered", "values": [{"min": 0, "max": 1000, "price": 0}, {"min": 1001, "max": 5000, "price": 0.001}, {"min": 5001, "max": null, "price": 0.0008}]}
	 * 含义：0-1000次免费，1001-5000次每次0.001元，5001次以上每次0.0008元
	 */
	TIERED("阶梯定价"),

	/**
	 * 简单使用量定价计算
	 * 特点：所有使用量都按单价计费，无免费额度
	 * 适用场景：简单的按量付费功能
	 * 示例配置：{"type": "usage", "values": [{"min": 0, "max": null, "price": 0.001}]}
	 * 含义：每次使用0.001元，无免费额度
	 */
	USAGE("使用量定价"),

	/**
	 * 包量定价计算
	 * 特点：用户购买包，包内使用量不额外收费
	 * 适用场景：预付费包量服务
	 * 示例配置：{"type": "package", "values": [{"quantity": 10000, "price": 50}, {"quantity": 50000, "price": 200}]}
	 * 含义：10000次包50元，50000次包200元
	 */
	PACKAGE("包量定价"),

	/**
	 * 固定价格阶梯定价计算
	 * 特点：达到阶梯后按固定价格收费，不按单次计费
	 * 适用场景：大客户专属定价，提供更优惠的包段价格
	 * 示例配置：{"type": "tiered_fixed", "values": [{"min": 0, "max": 1000, "price": 0}, {"min": 1001, "max": 5000, "price": 10.0}, {"min": 5001, "max": null, "price": 30.0}]}
	 * 含义：0-1000次免费，1001-5000次固定收费10元，5001次以上固定收费30元
	 */
	TIERED_FIXED("固定阶梯定价");

	/**
	 * 定价配置类型描述
	 */
	private final String description;

	/**
	 * 判断是否需要免费额度
	 */
	public boolean hasFreeQuota() {
		return this == QUOTA || this == TIERED;
	}

	/**
	 * 判断是否支持阶梯计费
	 */
	public boolean supportsTiering() {
		return this == TIERED || this == TIERED_FIXED;
	}

	/**
	 * 判断是否为包量模式
	 */
	public boolean isPackageMode() {
		return this == PACKAGE;
	}
}
