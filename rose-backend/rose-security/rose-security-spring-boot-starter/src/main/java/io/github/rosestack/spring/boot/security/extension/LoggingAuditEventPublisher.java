package io.github.rosestack.spring.boot.security.extension;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于日志的审计事件发布默认实现
 */
@Slf4j
@Component
@ConditionalOnMissingBean(AuditEventPublisher.class)
@ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAuditEventPublisher implements AuditEventPublisher {
    @Override
    public void publish(AuditEvent event) {
        log.info(
                "AuditEvent type={}, user={}, ip={}, details={}",
                event.getType(),
                event.getUsername(),
                event.getIp(),
                event.getDetails());
    }
}
