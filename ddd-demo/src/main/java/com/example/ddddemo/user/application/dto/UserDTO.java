package com.example.ddddemo.user.application.dto;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * <p>
 * 用于在应用层和接口层之间传输用户数据
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class UserDTO {

    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
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
    private AddressDTO address;
    
    /** 用户状态：0-禁用，1-正常 */
    private Integer status;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;

    // 构造函数
    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String phone, String realName,
                   String nickname, String avatar, Integer gender, LocalDateTime birthday,
                   AddressDTO address, Integer status, LocalDateTime lastLoginTime,
                   LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.realName = realName;
        this.nickname = nickname;
        this.avatar = avatar;
        this.gender = gender;
        this.birthday = birthday;
        this.address = address;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", realName='" + realName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender=" + gender +
                ", birthday=" + birthday +
                ", address=" + address +
                ", status=" + status +
                ", lastLoginTime=" + lastLoginTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
} 