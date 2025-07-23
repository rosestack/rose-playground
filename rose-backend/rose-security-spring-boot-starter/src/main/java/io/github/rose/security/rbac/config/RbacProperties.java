package io.github.rose.security.rbac.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * RBAC权限系统配置属性
 * <p>
 * 提供权限系统的完整配置选项，支持缓存、审计、监控等功能的配置。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.security.rbac")
public class RbacProperties {

    /**
     * 是否启用RBAC权限系统
     */
    private boolean enabled = true;

    /**
     * 是否启用权限继承
     */
    private boolean inheritanceEnabled = true;

    /**
     * 是否启用权限审计
     */
    private boolean auditEnabled = true;

    /**
     * 是否启用性能监控
     */
    private boolean metricsEnabled = true;

    /**
     * 默认权限检查超时时间
     */
    private Duration permissionCheckTimeout = Duration.ofSeconds(5);

    /**
     * 缓存配置
     */
    @NestedConfigurationProperty
    private CacheConfig cache = new CacheConfig();

    /**
     * 审计配置
     */
    @NestedConfigurationProperty
    private AuditConfig audit = new AuditConfig();

    /**
     * 权限继承配置
     */
    @NestedConfigurationProperty
    private InheritanceConfig inheritance = new InheritanceConfig();

    /**
     * 动态权限配置
     */
    @NestedConfigurationProperty
    private DynamicPermissionConfig dynamicPermission = new DynamicPermissionConfig();

    /**
     * 预定义权限配置
     */
    private Map<String, PermissionDefinition> predefinedPermissions;

    /**
     * 预定义角色配置
     */
    private Map<String, RoleDefinition> predefinedRoles;

    @Data
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;

        /**
         * 本地缓存配置
         */
        @NestedConfigurationProperty
        private LocalCacheConfig local = new LocalCacheConfig();

        /**
         * Redis缓存配置
         */
        @NestedConfigurationProperty
        private RedisCacheConfig redis = new RedisCacheConfig();

        /**
         * 是否启用缓存预热
         */
        private boolean preloadEnabled = true;

        /**
         * 缓存预热延迟时间
         */
        private Duration preloadDelay = Duration.ofSeconds(30);
    }

    @Data
    public static class LocalCacheConfig {
        /**
         * 用户权限缓存最大大小
         */
        private long userPermissionMaxSize = 10000;

        /**
         * 角色权限缓存最大大小
         */
        private long rolePermissionMaxSize = 5000;

        /**
         * 用户权限缓存过期时间
         */
        private Duration userPermissionExpireTime = Duration.ofMinutes(30);

        /**
         * 角色权限缓存过期时间
         */
        private Duration rolePermissionExpireTime = Duration.ofHours(1);
    }

    @Data
    public static class RedisCacheConfig {
        /**
         * Redis缓存过期时间
         */
        private Duration expireTime = Duration.ofHours(2);

        /**
         * 缓存键前缀
         */
        private String keyPrefix = "rbac:";

        /**
         * 是否启用缓存压缩
         */
        private boolean compressionEnabled = true;
    }

    @Data
    public static class AuditConfig {
        /**
         * 是否记录权限检查日志
         */
        private boolean logPermissionCheck = true;

        /**
         * 是否记录权限变更日志
         */
        private boolean logPermissionChange = true;

        /**
         * 是否记录角色变更日志
         */
        private boolean logRoleChange = true;

        /**
         * 审计日志保留天数
         */
        private int retentionDays = 90;

        /**
         * 是否启用异步审计
         */
        private boolean asyncEnabled = true;

        /**
         * 审计事件队列大小
         */
        private int queueSize = 10000;
    }

    @Data
    public static class InheritanceConfig {
        /**
         * 最大继承层级
         */
        private int maxDepth = 10;

        /**
         * 是否启用循环检测
         */
        private boolean circularDetectionEnabled = true;

        /**
         * 继承关系缓存时间
         */
        private Duration cacheTime = Duration.ofHours(1);
    }

    @Data
    public static class DynamicPermissionConfig {
        /**
         * 是否启用动态权限
         */
        private boolean enabled = true;

        /**
         * 动态权限刷新间隔
         */
        private Duration refreshInterval = Duration.ofMinutes(5);

        /**
         * 动态权限提供者类型
         */
        private List<String> providerTypes = List.of("database", "redis", "http");
    }

    @Data
    public static class PermissionDefinition {
        private String name;
        private String description;
        private String category;
        private String module;
        private boolean enabled = true;
        private List<String> dependencies;
    }

    @Data
    public static class RoleDefinition {
        private String name;
        private String description;
        private List<String> permissions;
        private List<String> inheritFrom;
        private boolean enabled = true;
    }
}