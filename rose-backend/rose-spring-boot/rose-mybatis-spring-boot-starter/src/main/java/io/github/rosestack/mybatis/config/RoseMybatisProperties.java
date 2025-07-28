package io.github.rosestack.mybatis.config;

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
     * SQL 性能监控配置
     */
    private Performance performance = new Performance();

    /**
     * 字段加密配置
     */
    private Encryption encryption = new Encryption();

    /**
     * 数据权限配置
     */
    private DataPermission dataPermission = new DataPermission();

    /**
     * 数据脱敏配置
     */
    private Desensitization desensitization = new Desensitization();

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

        /**
         * 更新时间字段名
         */
        private String updateTimeColumn = "updated_time";
    }

    /**
     * SQL 性能监控配置
     */
    @Data
    public static class Performance {
        /**
         * 是否启用 SQL 性能监控
         */
        private boolean enabled = true;

        /**
         * 慢查询阈值（毫秒）
         */
        private long slowSqlThreshold = 1000L;

        /**
         * 是否格式化 SQL
         */
        private boolean formatSql = true;
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
        private String secretKey;

        /**
         * 加密失败时是否抛出异常
         */
        private boolean failOnError = true;

        /**
         * 默认加密算法
         */
        private String defaultAlgorithm = "AES";

        /**
         * 是否使用 InnerInterceptor 模式
         * <p>
         * true: 使用 MyBatis Plus 的 InnerInterceptor，提供更好的性能和集成性
         * false: 使用传统的 MyBatis Interceptor
         * </p>
         */
        private boolean useInnerInterceptor = true;

        /**
         * 加密盐值（用于哈希字段）
         */
        private String salt = "DEFAULT_APP_SALT_2024";
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
    }

    /**
     * 数据脱敏配置
     */
    @Data
    public static class Desensitization {
        /**
         * 是否启用数据脱敏
         */
        private boolean enabled = true;
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
         * 是否包含参数
         */
        private boolean includeParameters = false;

        /**
         * 日志级别
         */
        private String logLevel = "INFO";
    }
}
