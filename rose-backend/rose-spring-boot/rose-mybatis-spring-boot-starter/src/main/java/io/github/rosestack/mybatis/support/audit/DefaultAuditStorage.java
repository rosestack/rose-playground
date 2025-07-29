package io.github.rosestack.mybatis.support.audit;

import io.github.rosestack.core.spring.SpringBeanUtils;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DefaultAuditStorage implements AuditStorage {

    @Override
    public void save(AuditLogEntry auditLogEntry) {
        // 发布审计事件
        publishAuditEvent(auditLogEntry);

        // 简单的日志输出实现
        // 实际项目中可以：
        // 1. 存储到数据库表 audit_log
        // 2. 发送到消息队列 (Kafka, RabbitMQ)
        // 3. 写入文件或日志收集系统 (ELK)
        // 4. 发送到监控系统 (Prometheus, Grafana)

        log.info("审计日志已记录: {}", auditLogEntry);

        // 如果有字段变更，额外记录详细信息
        if (auditLogEntry.getFieldChanges() != null && !auditLogEntry.getFieldChanges().isEmpty()) {
            log.info("字段变更详情: {}", auditLogEntry.formatFieldChanges());
        }
    }

    /**
     * 发布审计事件
     */
    private void publishAuditEvent(AuditLogEntry event) {
        try {
            SpringBeanUtils.getApplicationContext().publishEvent(event);
            log.debug("审计事件已发布: {}", event);
        } catch (Exception e) {
            log.warn("发布审计事件失败: {}", e.getMessage(), e);
        }
    }
}
