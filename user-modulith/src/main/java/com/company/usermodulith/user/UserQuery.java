package com.company.usermodulith.user;

import lombok.Data;

/**
 * 用户分页查询请求
 * <p>
 * 自定义分页请求对象，不依赖任何 ORM 框架
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class UserQuery {

    /**
     * 用户名（可选，支持模糊查询）
     */
    private String username;

    /**
     * 邮箱（可选，支持模糊查询）
     */
    private String email;

    private String phone;

    /**
     * 状态（可选）
     */
    private String status;
} 