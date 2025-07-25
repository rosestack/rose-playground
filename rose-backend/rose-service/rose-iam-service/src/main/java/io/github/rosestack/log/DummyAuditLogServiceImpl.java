package io.github.rosestack.log;

import io.github.rosestack.core.model.HasName;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@ConditionalOnProperty(prefix = "audit-log", value = "enabled", havingValue = "false")
public class DummyAuditLogServiceImpl implements AuditLogService {
    public <E extends HasName, I extends Serializable> void logEntityAction(String userId, String userName, I entityId, E entity, ActionType actionType, Exception e, Object... additionalInfo) {

    }
}
