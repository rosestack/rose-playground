package io.github.rosestack.iam.domain;

import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;

@Data
public class UserDepartment extends BaseEntity {
    private String departmentId;

    private String userId;
}
