package io.github.rose.user.domain;

import io.github.rose.common.model.BaseAudit;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserGroup extends BaseAudit<Long> {

    private String groupId;

    private String userId;
}
