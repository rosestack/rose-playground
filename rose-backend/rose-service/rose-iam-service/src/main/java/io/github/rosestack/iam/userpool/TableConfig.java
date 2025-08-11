package io.github.rosestack.iam.userpool;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TableConfig {
    private String id;
    private String name;
    private String description;
    private Boolean accessControl;
    private Boolean adminVisible;
    private String dataType;
    private Boolean desensitization;
    private Boolean encrypted;
    private Boolean everEncrypted;
    private String key;
    private String options;
    private String targetType; //user,role,department
    private String tenantId;
    private String userPoolId;
    private String userVisible;

    private Boolean isBase; //是否基础字段

    private Boolean mask;

    //{type": "UNIQUE","content": "", "error": "用户名重复"}
    private String validateRules;

    private String width;
}
