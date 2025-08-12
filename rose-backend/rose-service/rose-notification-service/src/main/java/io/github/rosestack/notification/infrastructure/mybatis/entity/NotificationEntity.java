package io.github.rosestack.notification.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.domain.value.NotificationStatus;
import io.github.rosestack.notification.domain.value.TargetType;
import io.github.rosestack.notification.infrastructure.mybatis.typehandler.NotificationChannelTypeHandler;
import io.github.rosestack.notification.infrastructure.mybatis.typehandler.NotificationStatusTypeHandler;
import io.github.rosestack.notification.infrastructure.mybatis.typehandler.TargetTypeTypeHandler;
import java.time.LocalDateTime;
import lombok.Data;

/** Notification 持久化对象 */
@Data
@TableName("notification")
public class NotificationEntity {
    private String id;
    private String tenantId;
    private String channelId;
    private String templateId;
    private String target;

    @TableField(typeHandler = TargetTypeTypeHandler.class)
    private TargetType targetType;

    private String content;

    @TableField(typeHandler = NotificationStatusTypeHandler.class)
    private NotificationStatus status;

    @TableField(typeHandler = NotificationChannelTypeHandler.class)
    private NotificationChannelType channelType;

    private String failReason;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
    private int retryCount;
    private String traceId;
    private String requestId;
}
