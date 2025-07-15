package io.github.rose.log;

import java.io.Serializable;

public interface AuditLogService {
    <E extends HasName, I extends Serializable> void logEntityAction(
            String userId,
            String userName,
            I entityId,
            E entity,
            ActionType actionType,
            Exception e, Object... additionalInfo);
}
