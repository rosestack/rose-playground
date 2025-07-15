package io.github.rose.notification.infra.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * NotificationTemplate 持久化对象
 */
@Data
@TableName("notification_template")
public class NotificationTemplateEntity {
    private String id;
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private String type;
    private String content;
    private boolean enabled;
    private int version;
    private String lang;
}
