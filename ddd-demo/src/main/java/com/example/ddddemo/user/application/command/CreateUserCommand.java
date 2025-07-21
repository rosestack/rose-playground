package com.example.ddddemo.user.application.command;

/**
 * 创建用户命令
 * <p>
 * 用于创建用户的命令对象
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class CreateUserCommand {

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

    // 构造函数
    public CreateUserCommand() {}

    public CreateUserCommand(String username, String email, String phone, String password, String realName) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.realName = realName;
    }

    // Getter和Setter方法
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
} 