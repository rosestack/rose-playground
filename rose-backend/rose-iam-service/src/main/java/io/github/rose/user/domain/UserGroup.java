package io.github.rose.user.domain;

import io.github.rose.core.model.AuditModel;
import lombok.Data;

@Data
public class UserGroup extends AuditModel<Long> {

    private String groupId;

    private String userId;
}
