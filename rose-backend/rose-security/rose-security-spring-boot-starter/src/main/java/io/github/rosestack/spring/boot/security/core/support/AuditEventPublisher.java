package io.github.rosestack.spring.boot.security.core.support;

/**
 * 审计事件发布接口
 */
public interface AuditEventPublisher {
    void publish(AuditEvent event);
}
