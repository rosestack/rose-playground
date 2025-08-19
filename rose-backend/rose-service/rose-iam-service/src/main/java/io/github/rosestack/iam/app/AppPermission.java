package io.github.rosestack.iam.app;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AppPermission {
    private Long id;

    private String appId;

    // user,group,role,org
    private String targetType;

    private String targetId;

    /**
     * 当主体类型为 "ORG" 时，授权是否被子节点继承
     */
    private Boolean inheritByChildren;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 主体类型
     */
    public static enum TargetType {
        USER("USER"),
        ROLE("ROLE"),
        GROUP("GROUP"),
        ORG("ORG"),
        TENANT("TENANT"),
        ;

        private String value;

        TargetType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
