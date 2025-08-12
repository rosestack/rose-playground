package io.github.rosestack.notification.domain.entity;

import lombok.Data;

/** 通知模板聚合根 */
@Data
public class NotificationTemplate {
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
