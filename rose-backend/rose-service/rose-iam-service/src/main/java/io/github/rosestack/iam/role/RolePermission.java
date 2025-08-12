package io.github.rosestack.iam.role;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RolePermission {
    private Long id;

    private String roleId;

    // user,group,department
    private String targetType;

    private String targetId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
