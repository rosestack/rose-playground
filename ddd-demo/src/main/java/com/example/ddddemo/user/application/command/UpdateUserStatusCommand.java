package com.example.ddddemo.user.application.command;

/**
 * 更新用户状态命令
 * <p>
 * 用于更新用户状态的命令对象
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class UpdateUserStatusCommand {

    /** 用户状态：0-禁用，1-正常 */
    private Integer status;

    // 构造函数
    public UpdateUserStatusCommand() {}

    public UpdateUserStatusCommand(Integer status) {
        this.status = status;
    }

    // Getter和Setter方法
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
} 