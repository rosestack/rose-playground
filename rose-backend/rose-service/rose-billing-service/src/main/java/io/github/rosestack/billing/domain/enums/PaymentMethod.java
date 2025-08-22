package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付方式枚举
 * 
 * 定义系统中支持的支付方式
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    /**
     * 信用卡 - 最常用的在线支付方式
     * 支持自动续费
     */
    CREDIT_CARD("信用卡"),

    /**
     * 借记卡 - 直接从银行账户扣款
     * 支持自动续费
     */
    DEBIT_CARD("借记卡"),

    /**
     * PayPal - 第三方支付平台
     * 支持自动续费
     */
    PAYPAL("PayPal"),

    /**
     * 银行转账 - 企业客户常用
     * 通常需要手动处理
     */
    BANK_TRANSFER("银行转账"),

    /**
     * 电子钱包 - 如支付宝、微信支付等
     * 适用于特定地区
     */
    WALLET("电子钱包"),

    /**
     * 其他 - 其他支付方式
     * 需要在 metadata 中记录具体方式
     */
    OTHER("其他");

    /**
     * 支付方式描述
     */
    private final String description;

    /**
     * 判断是否支持自动续费
     */
    public boolean supportsAutoRenewal() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PAYPAL;
    }

    /**
     * 判断是否为在线支付
     */
    public boolean isOnlinePayment() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PAYPAL || this == WALLET;
    }

    /**
     * 判断是否需要手动处理
     */
    public boolean requiresManualProcessing() {
        return this == BANK_TRANSFER || this == OTHER;
    }

    /**
     * 判断是否为企业级支付方式
     */
    public boolean isEnterprisePayment() {
        return this == BANK_TRANSFER;
    }
}