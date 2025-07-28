package io.github.rosestack.mybatis.enums;

/**
 * 脱敏类型枚举
 */
public enum SensitiveType {
    /**
     * 手机号脱敏：138****8000
     */
    PHONE,

    /**
     * 身份证号脱敏：110101****1234
     */
    ID_CARD,

    /**
     * 邮箱脱敏：abc***@example.com
     */
    EMAIL,

    /**
     * 银行卡号脱敏：6222****1234
     */
    BANK_CARD,

    PLATE_CARD,

    /**
     * 姓名脱敏：张*三
     */
    NAME,

    /**
     * 地址脱敏：北京市***区
     */
    ADDRESS,

    SECRET,

    /**
     * 自定义脱敏规则
     */
    CUSTOM
}