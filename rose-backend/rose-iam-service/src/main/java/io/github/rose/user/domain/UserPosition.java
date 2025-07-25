package io.github.rose.user.domain;

import io.github.rose.core.entity.BaseEntity;
import lombok.Data;

@Data
public class UserPosition extends BaseEntity {
    private String positionId;

    private String userId;
}
