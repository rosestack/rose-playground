package io.github.rosestack.iam.role;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RolePermission {
    private Long id;

    private String roleId;

    // user,group,department
    private String targetType;

    private String targetId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
