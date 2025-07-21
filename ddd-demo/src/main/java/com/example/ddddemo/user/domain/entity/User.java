package com.example.ddddemo.user.domain.entity;

import com.example.ddddemo.shared.domain.entity.AggregateRoot;
import com.example.ddddemo.user.domain.event.UserCreatedEvent;
import com.example.ddddemo.user.domain.event.UserUpdatedEvent;
import com.example.ddddemo.user.domain.valueobject.Address;

import java.time.LocalDateTime;

/**
 * 用户领域实体
 * <p>
 * 用户聚合根，负责用户相关的业务逻辑和状态管理
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>用户基本信息管理</li>
 *   <li>用户状态管理</li>
 *   <li>业务规则验证</li>
 *   <li>领域事件发布</li>
 * </ul>
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class User extends AggregateRoot<Long> {

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
    
    /** 地址 */
    private Address address;
    
    /** 用户状态：0-禁用，1-正常 */
    private Integer status;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 私有构造函数，防止外部直接创建
     */
    private User() {
        super();
    }

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param email 邮箱
     * @param phone 手机号
     * @param password 密码
     * @param realName 真实姓名
     * @return 用户实例
     */
    public static User create(String username, String email, String phone, String password, String realName) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.phone = phone;
        user.password = password;
        user.realName = realName;
        user.status = 1; // 默认正常状态
        user.createTime = LocalDateTime.now();
        user.updateTime = LocalDateTime.now();
        
        // 发布用户创建事件
        user.addDomainEvent(new UserCreatedEvent(user));
        
        return user;
    }

    /**
     * 更新用户基本信息
     *
     * @param realName 真实姓名
     * @param nickname 昵称
     * @param avatar 头像URL
     * @param gender 性别
     * @param birthday 生日
     * @param address 地址
     */
    public void updateBasicInfo(String realName, String nickname, String avatar, 
                               Integer gender, LocalDateTime birthday, Address address) {
        this.realName = realName;
        this.nickname = nickname;
        this.avatar = avatar;
        this.gender = gender;
        this.birthday = birthday;
        this.address = address;
        this.updateTime = LocalDateTime.now();
        
        // 发布用户更新事件
        addDomainEvent(new UserUpdatedEvent(this));
    }

    /**
     * 更新用户状态
     *
     * @param status 新状态
     */
    public void updateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("用户状态只能是0(禁用)或1(正常)");
        }
        
        this.status = status;
        this.updateTime = LocalDateTime.now();
        
        // 发布用户更新事件
        addDomainEvent(new UserUpdatedEvent(this));
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查用户是否可用
     *
     * @return 是否可用
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * 检查用户是否被禁用
     *
     * @return 是否被禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
    }

    /**
     * 获取用户显示名称
     *
     * @return 显示名称（优先昵称，其次真实姓名，最后用户名）
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        return username;
    }

    // Getter方法
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getRealName() {
        return realName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getGender() {
        return gender;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public Address getAddress() {
        return address;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
} 