package io.github.rosestack.notification.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.infrastructure.mybatis.typehandler.NotificationChannelTypeHandler;
import lombok.Data;

import java.util.Map;

/**
 * NotificationChannel 持久化对象
 */
@Data
@TableName("notification_channel")
public class NotificationChannelEntity {
    private String id;
    private String tenantId;

    @TableField(typeHandler = NotificationChannelTypeHandler.class)
    private NotificationChannelType channelType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    private boolean enabled;
}
