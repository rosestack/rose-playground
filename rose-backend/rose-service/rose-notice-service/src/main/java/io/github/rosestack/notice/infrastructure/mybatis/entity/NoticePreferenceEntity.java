package io.github.rosestack.notice.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.domain.value.TimeWindow;
import io.github.rosestack.notice.infrastructure.mybatis.typehandler.NoticeChannelTypeHandler;
import lombok.Data;

/**
 * NoticePreference 持久化对象
 */
@Data
@TableName("notice_preference")
public class NoticePreferenceEntity {
    private String id;
    private String tenantId;
    private String userId;

    @TableField(typeHandler = NoticeChannelTypeHandler.class)
    private NoticeChannelType channelType;

    private boolean enabled;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private TimeWindow quietPeriod;

    private String channelBlacklist;
    private String channelWhitelist;
    private Integer frequencyLimit;
}
