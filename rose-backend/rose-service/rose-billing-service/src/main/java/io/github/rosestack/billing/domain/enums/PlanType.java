package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 套餐类型枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PlanType {

	/**
	 * 免费版 - 基础功能，适合个人用户试用
	 */
	FREE("免费版"),

	/**
	 * 基础版 - 基本付费套餐，适合小团队
	 */
	BASIC("基础版"),

	/**
	 * 专业版 - 完整功能套餐，适合成长型企业
	 */
	PRO("专业版"),

	/**
	 * 企业版 - 高级功能套餐，适合大型企业
	 */
	ENTERPRISE("企业版");

	private final String description;
}
