package io.github.rose.user.domain;

import io.github.rose.core.entity.BaseEntity;
import lombok.Data;

@Data
public class UserDepartment extends BaseEntity {
    private String departmentId;

    private String userId;
}
