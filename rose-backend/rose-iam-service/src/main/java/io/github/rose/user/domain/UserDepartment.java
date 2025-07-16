package io.github.rose.user.domain;

import io.github.rose.common.model.BaseAudit;
import lombok.Data;

@Data
public class UserDepartment extends BaseAudit<Long> {
    private String departmentId;

    private String userId;
}
