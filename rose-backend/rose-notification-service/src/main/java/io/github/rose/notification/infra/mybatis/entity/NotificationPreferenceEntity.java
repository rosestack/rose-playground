package io.github.rose.notification.infra.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.domain.value.TimeWindow;
import io.github.rose.notification.infra.mybatis.typehandler.NotificationChannelTypeHandler;
import lombok.Data;

/**
 * NotificationPreference 持久化对象
 */
@Data
@TableName("notification_preference")
public class NotificationPreferenceEntity {
    private String id;
    private String tenantId;
    private String userId;
    @TableField(typeHandler = NotificationChannelTypeHandler.class)
    private NotificationChannelType channelType;
    private boolean enabled;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private TimeWindow quietPeriod;
    private String channelBlacklist;
    private String channelWhitelist;
    private Integer frequencyLimit;
}
