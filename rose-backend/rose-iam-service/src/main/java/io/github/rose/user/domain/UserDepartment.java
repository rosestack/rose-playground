package io.github.rose.user.domain;

import io.github.rose.core.model.AuditModel;
import io.github.rose.core.model.Auditable;
import lombok.Data;

@Data
public class UserDepartment extends AuditModel<Long> {
    private String departmentId;

    private String userId;
}
