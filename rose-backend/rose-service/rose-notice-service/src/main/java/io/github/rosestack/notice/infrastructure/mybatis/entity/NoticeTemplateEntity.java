package io.github.rosestack.notice.infrastructure.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * NoticeTemplate 持久化对象
 */
@Data
@TableName("notice_template")
public class NoticeTemplateEntity {
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
