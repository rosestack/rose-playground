package io.github.rosestack.notification.domain.entity;

import io.github.rosestack.notification.domain.value.NotificationChannelType;
import java.util.Map;
import lombok.Data;

/**
 * 渠道配置聚合根
 */
@Data
public class NotificationChannel {
    private String id;
    private String tenantId;
    private NotificationChannelType channelType;
    private Map<String, Object> config;
    private boolean enabled;

    /**
     * 通道优先级，数值越小优先级越高
     */
    private Integer priority;
}
