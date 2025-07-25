package io.github.rose.notification.domain.entity;

import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.domain.value.NotificationStatus;
import io.github.rose.notification.domain.value.TargetType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知聚合根
 */
@Data
public class Notification {
    private String id;
    private String tenantId;
    private String channelId;
    private String templateId;
    private String target;
    private TargetType targetType;
    private String content;
    private NotificationChannelType channelType;

    private String requestId;
    private NotificationStatus status;

    private String failReason;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;

    private LocalDateTime recallTime;

    private String traceId;
}
