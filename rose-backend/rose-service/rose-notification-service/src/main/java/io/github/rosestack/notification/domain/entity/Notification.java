package io.github.rosestack.notification.domain.entity;

import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.domain.value.NotificationStatus;
import io.github.rosestack.notification.domain.value.TargetType;
import java.time.LocalDateTime;
import lombok.Data;

/** 通知聚合根 */
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
