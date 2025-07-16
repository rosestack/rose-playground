package io.github.rose.user.domain;

import io.github.rose.common.model.BaseAudit;
import lombok.Data;

@Data
public class UserGroup extends BaseAudit<Long> {

    private String groupId;

    private String userId;
}
