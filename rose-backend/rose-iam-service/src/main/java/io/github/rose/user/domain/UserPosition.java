package io.github.rose.user.domain;

import io.github.rose.core.model.AuditModel;
import lombok.Data;

@Data
public class UserPosition extends AuditModel<Long> {
    private String positionId;

    private String userId;
}
