package io.github.rosestack.iam.domain;

import io.github.rosestack.core.entity.BaseEntity;
import lombok.Data;

@Data
public class UserDepartment extends BaseEntity {
    private String departmentId;

    private String userId;
}
