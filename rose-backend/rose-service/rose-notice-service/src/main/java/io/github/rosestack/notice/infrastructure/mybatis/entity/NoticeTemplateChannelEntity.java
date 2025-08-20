package io.github.rosestack.notice.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 模板-渠道 多对多关联表实体
 */
@Data
@TableName("notice_template_channel")
public class NoticeTemplateChannelEntity {
    private Long id;
    private String templateId;
    private String channelId;
}
