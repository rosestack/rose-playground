package com.example.ddddemo.user.application.command;

import com.example.ddddemo.user.application.dto.AddressDTO;

import java.time.LocalDateTime;

/**
 * 更新用户命令
 * <p>
 * 用于更新用户信息的命令对象
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class UpdateUserCommand {

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

    // 构造函数
    public UpdateUserCommand() {}

    // Getter和Setter方法
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
} 