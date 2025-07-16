package io.github.rose.log;

<<<<<<< HEAD
import io.github.rose.core.model.HasName;

=======
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
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
