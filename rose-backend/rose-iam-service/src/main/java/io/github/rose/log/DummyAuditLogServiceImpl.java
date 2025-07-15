package io.github.rose.log;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@ConditionalOnProperty(prefix = "audit-log", value = "enabled", havingValue = "false")
public class DummyAuditLogServiceImpl implements AuditLogService {
    @Override
    public <E extends HasName, I extends Serializable> void logEntityAction(String userId, String userName, I entityId, E entity, com.chensoul.authing.auditlog.ActionType actionType, Exception e, Object... additionalInfo) {

    }
}
