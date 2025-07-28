package io.github.rosestack.mybatis.audit;

import io.github.rosestack.mybatis.interceptor.AuditInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认审计存储实现
 * <p>
 * 简单的日志输出实现，实际项目中可以替换为数据库存储、消息队列等。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class DefaultAuditStorage implements AuditInterceptor.AuditStorage {

    @Override
    public void save(AuditInterceptor.AuditLog auditLog) {
        // 简单的日志输出实现
        // 实际项目中可以：
        // 1. 存储到数据库表 audit_log
        // 2. 发送到消息队列 (Kafka, RabbitMQ)
        // 3. 写入文件或日志收集系统 (ELK)
        // 4. 发送到监控系统 (Prometheus, Grafana)
        
        log.info("审计日志已记录: {}", formatAuditSummary(auditLog));
        
        // 如果有字段变更，额外记录详细信息
        if (auditLog.getFieldChanges() != null && !auditLog.getFieldChanges().isEmpty()) {
            log.info("字段变更详情: {}", formatFieldChanges(auditLog.getFieldChanges()));
        }
    }

    private String formatAuditSummary(AuditInterceptor.AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();
        sb.append("操作: ").append(auditLog.getOperation())
          .append(", 映射器: ").append(auditLog.getMapperId())
          .append(", 执行时间: ").append(auditLog.getExecutionTime()).append("ms")
          .append(", 成功: ").append(auditLog.isSuccess())
          .append(", 用户: ").append(auditLog.getUserId())
          .append(", 租户: ").append(auditLog.getTenantId());
        
        if (auditLog.getEntityClass() != null) {
            sb.append(", 实体: ").append(auditLog.getEntityClass())
              .append("[").append(auditLog.getEntityId()).append("]");
        }
        
        if (auditLog.getModule() != null) {
            sb.append(", 业务: ").append(auditLog.getModule())
              .append(".").append(auditLog.getBusinessOperation());
        }
        
        return sb.toString();
    }

    private String formatFieldChanges(java.util.List<AuditInterceptor.FieldChange> fieldChanges) {
        StringBuilder sb = new StringBuilder();
        for (AuditInterceptor.FieldChange change : fieldChanges) {
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
}
