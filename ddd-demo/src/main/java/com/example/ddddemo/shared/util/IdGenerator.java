package com.example.ddddemo.shared.util;

import java.util.UUID;

/**
 * ID生成器
 * <p>
 * 提供各种ID生成策略
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
public final class IdGenerator {

    private IdGenerator() {
        // 私有构造函数，防止实例化
    }

    /**
     * 生成UUID
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成不带连字符的UUID
     */
    public static String generateUuidWithoutHyphens() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成时间戳ID
     */
    public static String generateTimestampId() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 生成纳秒ID
     */
    public static String generateNanoId() {
        return String.valueOf(System.nanoTime());
    }
}