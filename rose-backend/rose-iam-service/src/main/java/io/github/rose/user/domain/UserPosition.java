package io.github.rose.user.domain;

<<<<<<< HEAD
import io.github.rose.core.model.BaseAudit;
import lombok.Data;

=======
import io.github.rose.common.model.BaseAudit;
import lombok.Data;

import java.time.LocalDateTime;

>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
@Data
public class UserPosition extends BaseAudit<Long> {
    private String positionId;

    private String userId;
}
