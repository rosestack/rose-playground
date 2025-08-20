package io.github.rosestack.notice.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.infrastructure.mybatis.typehandler.NoticeChannelTypeHandler;
import java.util.Map;
import lombok.Data;

/**
 * NoticeChannel 持久化对象
 */
@Data
@TableName("notice_channel")
public class NoticeChannelEntity {
    private String id;
    private String tenantId;

    @TableField(typeHandler = NoticeChannelTypeHandler.class)
    private NoticeChannelType channelType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    private boolean enabled;
}
