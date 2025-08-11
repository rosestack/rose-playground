package com.company.usermodulith.user.internal;

/**
 * 用户模块异常
 *
 * @author Chen Soul
 * @since 1.0.0
 */
public class UserException extends RuntimeException {

    /**
     * 构造用户异常
     *
     * @param message 异常消息
     */
    public UserException(String message) {
        super(message);
    }

    /**
     * 构造用户异常
     *
     * @param message 异常消息
     * @param cause 原始异常
     */
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 用户名已存在异常
     *
     * @param username 用户名
     * @return 用户异常
     */
    public static UserException usernameAlreadyExists(String username) {
        return new UserException("用户名已存在: " + username);
    }

    /**
     * 邮箱已存在异常
     *
     * @param email 邮箱
     * @return 用户异常
     */
    public static UserException emailAlreadyExists(String email) {
        return new UserException("邮箱已存在: " + email);
    }

    /**
     * 用户不存在异常
     *
     * @param id 用户ID
     * @return 用户异常
     */
    public static UserException userNotFound(Long id) {
        return new UserException("用户不存在: " + id);
    }
} 