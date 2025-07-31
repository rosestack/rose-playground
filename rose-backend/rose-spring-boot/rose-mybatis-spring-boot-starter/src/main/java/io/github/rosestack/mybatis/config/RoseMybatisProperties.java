package io.github.rosestack.mybatis.config;

import io.github.rosestack.mybatis.support.encryption.EncryptType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Rose MyBatis Plus 配置属性
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.mybatis")
public class RoseMybatisProperties {

    /**
     * 是否启用 Rose MyBatis Plus 增强功能
     */
    private boolean enabled = true;

    /**
     * 多租户配置
     */
    private Tenant tenant = new Tenant();

    /**
     * 分页配置
     */
    private Pagination pagination = new Pagination();


    /**
     * 乐观锁配置
     */
    private OptimisticLock optimisticLock = new OptimisticLock();

    /**
     * 字段填充配置
     */
    private FieldFill fieldFill = new FieldFill();

    /**
     * 字段加密配置
     */
    private Encryption encryption = new Encryption();

    /**
     * 数据权限配置
     */
    private DataPermission dataPermission = new DataPermission();

    /**
     * SQL 审计配置
     */
    private Audit audit = new Audit();

    /**
     * 多租户配置
     */
    @Data
    public static class Tenant {
        /**
         * 是否启用多租户
         */
        private boolean enabled = false;

        /**
         * 租户字段名
         */
        private String column = "tenant_id";

        /**
         * 忽略多租户的表名列表
         */
        private List<String> ignoreTables = new ArrayList<>();

        /**
         * 忽略多租户的表名前缀列表
         */
        private List<String> ignoreTablePrefixes = new ArrayList<>();
    }

    /**
     * 分页配置
     */
    @Data
    public static class Pagination {
        /**
         * 是否启用分页插件
         */
        private boolean enabled = true;

        /**
         * 单页最大限制数量
         */
        private Long maxLimit = 1000L;

        /**
         * 是否启用合理化分页
         */
        private boolean reasonable = true;

        /**
         * 数据库类型（自动检测）
         */
        private String dbType = "mysql";
    }


    /**
     * 乐观锁配置
     */
    @Data
    public static class OptimisticLock {
        /**
         * 是否启用乐观锁
         */
        private boolean enabled = true;

    }

    /**
     * 字段填充配置
     */
    @Data
    public static class FieldFill {
        /**
         * 是否启用字段自动填充
         */
        private boolean enabled = true;

        /**
         * 创建时间字段名
         */
        private String createTimeColumn = "created_time";

        private String createdByColumn = "created_by";

        /**
         * 更新时间字段名
         */
        private String updateTimeColumn = "updated_time";

        private String updatedByColumn = "updated_by";

        private String defaultUser;
    }

    /**
     * 字段加密配置
     */
    @Data
    public static class Encryption {
        /**
         * 是否启用字段加密
         */
        private boolean enabled = true;

        /**
         * 加密密钥（生产环境应该从外部配置或密钥管理系统获取）
         */
        private String secretKey = "0123456789abcdeffedcba9876543210";

        /**
         * 加密失败时是否抛出异常
         */
        private boolean failOnError = true;

        /**
         * 密钥轮换配置
         */
        private KeyRotation keyRotation = new KeyRotation();

        /**
         * 哈希配置
         */
        private Hash hash = new Hash();

        /**
         * 哈希配置
         */
        @Data
        public static class Hash {
            /**
             * 是否启用哈希功能
             */
            private boolean enabled = true;

            /**
             * 全局盐值（生产环境应该从外部配置获取）
             */
            private String globalSalt = "rose-mybatis-global-salt-2024";

            /**
             * 默认哈希算法
             */
            private String algorithm = "HMAC_SHA256";

            /**
             * HMAC 密钥（生产环境应该从外部配置获取）
             */
            private String hmacKey = "rose-mybatis-hmac-key-2024";
        }

        /**
         * 密钥轮换配置
         */
        @Data
        public static class KeyRotation {
            /**
             * 是否启用密钥轮换
             */
            private boolean enabled = false;

            /**
             * 自动轮换间隔（天）
             */
            private int autoRotationDays = 90;

            /**
             * 密钥保留期（天）- 旧密钥保留多久用于解密
             */
            private int keyRetentionDays = 30;

            /**
             * 是否启用自动清理过期密钥
             */
            private boolean autoCleanup = true;

            /**
             * 默认密钥长度配置
             */
            private KeyLength keyLength = new KeyLength();

            @Data
            public static class KeyLength {
                /**
                 * AES密钥长度（位）
                 */
                private int aes = 256;

                /**
                 * RSA密钥长度（位）
                 */
                private int rsa = 2048;
            }
        }
    }

    /**
     * 数据权限配置
     */
    @Data
    public static class DataPermission {
        /**
         * 是否启用数据权限
         */
        private boolean enabled = true;

        /**
         * 缓存配置
         */
        private Cache cache = new Cache();

        /**
         * 缓存配置
         */
        @Data
        public static class Cache {
            /**
             * 缓存过期时间（分钟）
             */
            private long expireMinutes = 30;

            /**
             * 缓存清理间隔（分钟）
             */
            private long cleanupIntervalMinutes = 60;

            /**
             * 过期率阈值
             */
            private Double expiredRate = 0.5;

            /**
             * 最大注解缓存数量
             */
            private int maxAnnotationCacheSize = 10000;

            /**
             * 最大权限缓存数量
             */
            private int maxPermissionCacheSize = 50000;
        }
    }

    /**
     * SQL 审计配置
     */
    @Data
    public static class Audit {
        /**
         * 是否启用 SQL 审计
         */
        private boolean enabled = true;

        /**
         * 是否包含 SQL 语句
         */
        private boolean includeSql = true;

        /**
         * 日志级别
         */
        private String logLevel = "INFO";
    }
}
