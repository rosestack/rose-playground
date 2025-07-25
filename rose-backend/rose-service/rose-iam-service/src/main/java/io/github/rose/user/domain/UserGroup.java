package io.github.rose.user.domain;

import io.github.rose.core.entity.BaseEntity;
import lombok.Data;

@Data
public class UserGroup extends BaseEntity {

    private String groupId;

    private String userId;
}
