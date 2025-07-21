package com.example.ddddemo.shared.constant;

/**
 * 错误码常量定义
 * <p>
 * 定义系统中所有的错误码，用于统一错误处理
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
public final class ErrorCode {

    private ErrorCode() {
        // 私有构造函数，防止实例化
    }

    // 通用错误码
    public static final String SUCCESS = "SUCCESS";
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";

    // 用户相关错误码
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USERNAME_EXISTS = "USERNAME_EXISTS";
    public static final String EMAIL_EXISTS = "EMAIL_EXISTS";
    public static final String INVALID_USER_STATUS = "INVALID_USER_STATUS";
    public static final String USER_CREATE_FAILED = "USER_CREATE_FAILED";
    public static final String USER_UPDATE_FAILED = "USER_UPDATE_FAILED";
    public static final String USER_DELETE_FAILED = "USER_DELETE_FAILED";
}