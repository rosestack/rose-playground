package io.github.rosestack.billing.domain.enums;

import io.github.rosestack.core.model.BaseEnum;

/**
 * 计费模式枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum BillingMode implements BaseEnum {

    /**
     * 预付费模式 - 先充值后使用，余额不足时停服
     * 特点：用户先向账户充值，然后消费余额
     * 适用场景：个人用户、小型企业、对成本控制要求严格的用户
     * 扣费逻辑：实时扣减账户余额，余额不足时停止服务
     */
    PREPAID("PREPAID", "预付费"),

    /**
     * 后付费模式 - 先使用后付款，定期生成账单
     * 特点：用户先使用服务，然后定期付款
     * 适用场景：企业客户、信用良好的用户、大客户
     * 扣费逻辑：累计使用量，周期性生成账单进行结算
     */
    POSTPAID("POSTPAID", "后付费"),

    /**
     * 混合模式 - 优先扣余额，不足部分记账
     * 特点：结合预付费和后付费的优点
     * 适用场景：灵活的企业客户、需要灵活付费方式的用户
     * 扣费逻辑：账户有余额时按预付费扣减，余额不足时按后付费记账
     */
    HYBRID("HYBRID", "混合模式");

    private final String code;
    private final String name;

    BillingMode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 检查是否为预付费模式
     */
    public boolean isPrepaid() {
        return this == PREPAID;
    }

    /**
     * 检查是否为后付费模式
     */
    public boolean isPostpaid() {
        return this == POSTPAID;
    }

    /**
     * 检查是否为混合模式
     */
    public boolean isHybrid() {
        return this == HYBRID;
    }

    /**
     * 检查是否支持余额扣减
     */
    public boolean supportsBalanceDeduction() {
        return this == PREPAID || this == HYBRID;
    }

    /**
     * 检查是否支持记账
     */
    public boolean supportsBilling() {
        return this == POSTPAID || this == HYBRID;
    }
}