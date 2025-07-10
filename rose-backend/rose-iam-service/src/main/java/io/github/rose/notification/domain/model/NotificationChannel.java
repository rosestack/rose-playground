package io.github.rose.notification.domain.model;

import io.github.rose.notification.domain.value.NotificationChannelType;
import lombok.Data;

import java.util.Map;

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
