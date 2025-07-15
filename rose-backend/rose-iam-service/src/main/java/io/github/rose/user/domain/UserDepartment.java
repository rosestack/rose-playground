package io.github.rose.user.domain;

import io.github.rose.common.model.BaseAudit;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDepartment extends BaseAudit<Long> {
    private String departmentId;

    private String userId;
}
