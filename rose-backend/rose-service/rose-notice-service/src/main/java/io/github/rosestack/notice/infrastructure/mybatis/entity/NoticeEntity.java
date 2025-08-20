package io.github.rosestack.notice.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.domain.value.NoticeStatus;
import io.github.rosestack.notice.domain.value.TargetType;
import io.github.rosestack.notice.infrastructure.mybatis.typehandler.NoticeChannelTypeHandler;
import io.github.rosestack.notice.infrastructure.mybatis.typehandler.NoticeStatusTypeHandler;
import io.github.rosestack.notice.infrastructure.mybatis.typehandler.TargetTypeTypeHandler;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Notice 持久化对象
 */
@Data
@TableName("notice")
public class NoticeEntity {
    private String id;
    private String tenantId;
    private String channelId;
    private String templateId;
    private String target;

    @TableField(typeHandler = TargetTypeTypeHandler.class)
    private TargetType targetType;

    private String content;

    @TableField(typeHandler = NoticeStatusTypeHandler.class)
    private NoticeStatus status;

    @TableField(typeHandler = NoticeChannelTypeHandler.class)
    private NoticeChannelType channelType;

    private String failReason;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
    private int retryCount;
    private String traceId;
    private String requestId;
}
