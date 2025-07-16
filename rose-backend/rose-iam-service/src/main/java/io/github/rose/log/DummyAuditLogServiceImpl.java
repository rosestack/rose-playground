package io.github.rose.log;

<<<<<<< HEAD
import io.github.rose.core.model.HasName;
=======
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@ConditionalOnProperty(prefix = "audit-log", value = "enabled", havingValue = "false")
public class DummyAuditLogServiceImpl implements AuditLogService {
    @Override
<<<<<<< HEAD
    public <E extends HasName, I extends Serializable> void logEntityAction(String userId, String userName, I entityId, E entity, ActionType actionType, Exception e, Object... additionalInfo) {
=======
    public <E extends HasName, I extends Serializable> void logEntityAction(String userId, String userName, I entityId, E entity, com.chensoul.authing.auditlog.ActionType actionType, Exception e, Object... additionalInfo) {
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)

    }
}
