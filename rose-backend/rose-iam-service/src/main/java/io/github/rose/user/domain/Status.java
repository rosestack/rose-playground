package io.github.rose.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户当前状态：
 * - Activated: 正常状态
 * - Suspended: 已停用
 * - Deactivated: 已禁用
 * - Resigned: 已离职
 * - Archived: 已归档
 */
@Getter
@AllArgsConstructor
public enum Status {
    SUSPENDED("Suspended"),
    RESIGNED("Resigned"),
    ACTIVATED("Activated"),
    ARCHIVED("Archived"),
    DEACTIVATED("Deactivated"),
    ;
    private String value;
}