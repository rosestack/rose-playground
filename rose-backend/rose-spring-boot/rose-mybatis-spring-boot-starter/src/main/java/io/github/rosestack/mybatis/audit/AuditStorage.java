package io.github.rosestack.mybatis.audit;

/**
 * 审计存储接口
 */
public interface AuditStorage {
    void save(AuditLogEntry auditLogEntry);
}