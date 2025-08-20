package io.github.rosestack.notice.domain.entity;

import lombok.Data;

/**
 * 通知模板聚合根
 */
@Data
public class NoticeTemplate {
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
