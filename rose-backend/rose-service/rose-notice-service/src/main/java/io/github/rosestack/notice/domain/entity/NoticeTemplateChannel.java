package io.github.rosestack.notice.domain.entity;

import lombok.Data;

/**
 * 模板-渠道 多对多关联实体
 */
@Data
public class NoticeTemplateChannel {
    private Long id;
    private String templateId;
    private String channelId;
}
