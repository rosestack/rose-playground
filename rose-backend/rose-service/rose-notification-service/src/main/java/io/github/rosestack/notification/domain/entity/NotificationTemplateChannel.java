package io.github.rosestack.notification.domain.entity;

import lombok.Data;

/**
 * 模板-渠道 多对多关联实体
 */
@Data
public class NotificationTemplateChannel {
    private Long id;
    private String templateId;
    private String channelId;
}
