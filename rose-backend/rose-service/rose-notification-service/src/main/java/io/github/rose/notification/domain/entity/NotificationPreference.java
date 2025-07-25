package io.github.rose.notification.domain.entity;

import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.domain.value.TimeWindow;
import lombok.Data;

/**
 * 用户通知偏好聚合根
 */
@Data
public class NotificationPreference {
    private String id;
    private String tenantId;
    private String userId;
    private String type;

    private NotificationChannelType channelType;

    private boolean enabled;

    private TimeWindow quietPeriod;

    /**
     * 通道黑名单（JSON）
     */
    private String channelBlacklist;
    /**
     * 通道白名单（JSON）
     */
    private String channelWhitelist;
    /**
     * 频率限制（如每小时最多 N 条）
     */
    private Integer frequencyLimit;
}
