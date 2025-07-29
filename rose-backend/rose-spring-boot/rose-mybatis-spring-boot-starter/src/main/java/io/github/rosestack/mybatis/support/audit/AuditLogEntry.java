package io.github.rosestack.mybatis.support.audit;

import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一审计日志实体
 */
@lombok.Data
@lombok.Builder
@ToString
public class AuditLogEntry {
    // SQL审计信息
    private LocalDateTime createdTime;
    private String mapperId;
    private String sql;
    private String sqlType;
    private String parameters;
    private long executionTime;
    private boolean success;
    private String errorMessage;

    // 上下文信息
    private String userId;
    private String tenantId;
    private String requestId;

    // 业务变更信息
    private String module;
    private String businessOperation;
    private String entityClass;
    private Object entityId;
    private List<FieldChange> fieldChanges;

    public String formatFieldChanges() {
        if (fieldChanges == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (AuditLogEntry.FieldChange change : fieldChanges) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(change.getFieldName())
                    .append(": ")
                    .append(change.getOldValue())
                    .append(" -> ")
                    .append(change.getNewValue());

            if (change.isSensitive()) {
                sb.append(" (敏感字段已脱敏)");
            }
        }
        return sb.toString();
    }

    /**
     * 字段变更记录
     */
    @lombok.Data
    @lombok.Builder
    public static class FieldChange {
        private String fieldName;
        private String fieldType;
        private String oldValue;
        private String newValue;
        private boolean sensitive; // 是否为敏感字段
    }
}