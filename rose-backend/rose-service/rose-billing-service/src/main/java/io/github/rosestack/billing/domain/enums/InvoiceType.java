package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 账单类型枚举
 * 
 * 定义系统中支持的账单类型
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum InvoiceType {

    /**
     * 订阅账单 - 套餐基础费用账单
     * 特点：套餐基础费用账单
     * 生成时机：每个计费周期开始时
     * 金额来源：套餐的月费或年费
     */
    SUBSCRIPTION("订阅账单"),

    /**
     * 使用量账单 - 按量计费功能的使用费用
     * 特点：按量计费功能的使用费用
     * 生成时机：计费周期结束后，根据实际使用量生成
     * 金额来源：超出免费额度的使用量费用
     */
    USAGE("使用量账单"),

    /**
     * 调整账单 - 价格调整、升级降级等产生的差额
     * 特点：价格调整、升级降级等产生的差额
     * 生成时机：套餐变更、价格调整时
     * 金额来源：新旧价格的差额
     */
    ADJUSTMENT("调整账单"),

    /**
     * 退款账单 - 退款记录
     * 特点：退款记录
     * 生成时机：用户申请退款时
     * 金额来源：负数，表示退还给用户的金额
     */
    REFUND("退款账单");

    /**
     * 账单类型描述
     */
    private final String description;

    /**
     * 判断是否为费用类账单（产生正费用）
     */
    public boolean isChargeInvoice() {
        return this == SUBSCRIPTION || this == USAGE || this == ADJUSTMENT;
    }

    /**
     * 判断是否为退款类账单（产生负费用）
     */
    public boolean isRefundInvoice() {
        return this == REFUND;
    }

    /**
     * 判断是否为周期性账单
     */
    public boolean isRecurringInvoice() {
        return this == SUBSCRIPTION || this == USAGE;
    }

    /**
     * 判断是否为一次性账单
     */
    public boolean isOneTimeInvoice() {
        return this == ADJUSTMENT || this == REFUND;
    }
}