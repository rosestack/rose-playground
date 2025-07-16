package io.github.rose.user.domain;

import io.github.rose.common.model.BaseAudit;
import lombok.Data;

@Data
public class UserPosition extends BaseAudit<Long> {
    private String positionId;

    private String userId;
}
