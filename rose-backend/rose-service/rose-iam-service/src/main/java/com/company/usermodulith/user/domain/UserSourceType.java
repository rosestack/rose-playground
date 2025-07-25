
package com.company.usermodulith.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 来源类型:
 * - `excel`: 通过 excel 导入
 * - `register`: 用户自主注册
 * - `adminCreated`: 管理员后台手动创建（包含使用管理 API 创建用户 ）
 * - `syncTask`: 同步中心的同步任务
 */
@Getter
@AllArgsConstructor
public enum UserSourceType {
    EXCEL("excel"),
    REGISTER("register"),
    ADMIN_CREATED("adminCreated"),
    SYNC("sync"),
    WORKFLOW("workflow");
    private String value;
}