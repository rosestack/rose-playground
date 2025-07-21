package com.example.ddddemo.shared.constant;

/**
 * 业务常量定义
 * <p>
 * 定义系统中业务相关的常量
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
public final class BusinessConstant {

    private BusinessConstant() {
        // 私有构造函数，防止实例化
    }

    // 用户相关常量
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 100;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int ADDRESS_MAX_LENGTH = 200;

    // 分页相关常量
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 缓存相关常量
    public static final String USER_CACHE_PREFIX = "user:";
    public static final int USER_CACHE_EXPIRE_SECONDS = 3600; // 1小时
}