package io.github.rosestack.notification.domain.value;

/** 通知状态枚举 */
public enum NotificationStatus {
    PENDING, // 待发送
    MQ_DELIVERED, // 投递到 MQ 成功
    SENT, // 已发送
    FAILED, // 发送失败
    RECALLED,
    READ // 已读
}
