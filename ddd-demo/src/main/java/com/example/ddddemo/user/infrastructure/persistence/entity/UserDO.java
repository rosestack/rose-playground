package com.example.ddddemo.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据对象
 * <p>
 * 用于数据库持久化的用户实体
 *
 * @author DDD Demo
 * @since 1.0.0
 */
@Data
@TableName("user")
public class UserDO {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;

    /** 生日 */
    private LocalDateTime birthday;

    /** 国家 */
    private String country;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区县 */
    private String district;

    /** 详细地址 */
    private String detailAddress;

    /** 邮政编码 */
    private String postalCode;

    /** 用户状态：0-禁用，1-正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标志：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
} 