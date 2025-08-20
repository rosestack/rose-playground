package io.github.rosestack.notice.domain.entity;

import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.domain.value.NoticeStatus;
import io.github.rosestack.notice.domain.value.TargetType;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 通知聚合根
 */
@Data
public class Notice {
    private String id;
    private String tenantId;
    private String channelId;
    private String templateId;
    private String target;
    private TargetType targetType;
    private String content;
    private NoticeChannelType channelType;

    private String requestId;
    private NoticeStatus status;

    private String failReason;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;

    private LocalDateTime recallTime;

    private String traceId;
}
