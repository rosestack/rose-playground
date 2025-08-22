package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 功能范围枚举
 * <p>
 * 定义功能配额的作用范围
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ValueScope {

	/**
	 * 按订阅共享 - 功能配额在整个订阅范围内共享
	 * 特点：整个团队/组织共享配额
	 * 示例：存储空间100GB，整个团队共享使用
	 * 适用场景：团队共享资源、组织级功能
	 */
	PER_SUBSCRIPTION("按订阅共享"),

	/**
	 * 按席位独立 - 功能配额按每个用户席位独立计算
	 * 特点：每个用户席位独立享有配额
	 * 示例：API调用1000次/月，每个用户独立拥有1000次
	 * 适用场景：个人使用功能、用户级权限
	 */
	PER_SEAT("按席位独立");

	private final String description;
}
