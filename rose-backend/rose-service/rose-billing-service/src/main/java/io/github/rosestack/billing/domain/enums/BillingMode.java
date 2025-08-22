package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 计费模式枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum BillingMode {

	/**
	 * 预付费模式 - 先充值后使用，余额不足时停服
	 * 特点：用户先向账户充值，然后消费余额
	 * 适用场景：个人用户、小型企业、对成本控制要求严格的用户
	 * 扣费逻辑：实时扣减账户余额，余额不足时停止服务
	 */
	PREPAID("预付费"),

	/**
	 * 后付费模式 - 先使用后付款，定期生成账单
	 * 特点：用户先使用服务，然后定期付款
	 * 适用场景：企业客户、信用良好的用户、大客户
	 * 扣费逻辑：累计使用量，周期性生成账单进行结算
	 */
	POSTPAID("后付费"),

	/**
	 * 混合模式 - 优先扣余额，不足部分记账
	 * 特点：结合预付费和后付费的优点
	 * 适用场景：灵活的企业客户、需要灵活付费方式的用户
	 * 扣费逻辑：账户有余额时按预付费扣减，余额不足时按后付费记账
	 */
	HYBRID("混合模式");

	/**
	 * 计费模式描述
	 */
	private final String description;
}
