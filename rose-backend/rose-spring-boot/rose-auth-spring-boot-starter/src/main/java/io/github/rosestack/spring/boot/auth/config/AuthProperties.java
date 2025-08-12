package io.github.rosestack.spring.boot.auth.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证模块配置属性
 *
 * <p>提供认证模块的所有配置选项，包括 JWT、OAuth2、安全策略等配置。
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.auth")
public class AuthProperties {

    /** 是否启用认证模块 */
    private boolean enabled = true;

    /** JWT 配置 */
    private Jwt jwt = new Jwt();

    /** OAuth2 配置 */
    private OAuth2 oauth2 = new OAuth2();

    /** 安全配置 */
    private Security security = new Security();

    /** 缓存配置 */
    private Cache cache = new Cache();

    /** CORS 配置 */
    private Cors cors = new Cors();

    /** JWT 配置类 */
    @Data
    public static class Jwt {
        /** JWT 密钥 */
        private String secret = "rose-auth-default-secret-key-change-in-production";

        /** 访问令牌过期时间 */
        private Duration accessTokenExpiration = Duration.ofHours(1);

        /** 刷新令牌过期时间 */
        private Duration refreshTokenExpiration = Duration.ofDays(7);

        /** JWT 发行者 */
        private String issuer = "rose-auth";

        /** JWT 受众 */
        private String audience = "rose-app";

        /** 令牌前缀 */
        private String tokenPrefix = "Bearer ";

        /** 请求头名称 */
        private String headerName = "Authorization";
    }

    /** OAuth2 配置类 */
    @Data
    public static class OAuth2 {
        /** OAuth2 客户端配置 */
        private Map<String, OAuth2Client> clients = new HashMap<>();

        /** 登录成功重定向URL */
        private String successRedirectUrl = "/";

        /** 登录失败重定向URL */
        private String failureRedirectUrl = "/login?error";
    }

    /** OAuth2 客户端配置 */
    @Data
    public static class OAuth2Client {
        /** 客户端ID */
        private String clientId;

        /** 客户端密钥 */
        private String clientSecret;

        /** 授权范围 */
        private String scope;

        /** 重定向URI */
        private String redirectUri;

        /** 授权URI */
        private String authorizationUri;

        /** 令牌URI */
        private String tokenUri;

        /** 用户信息URI */
        private String userInfoUri;

        /** 用户名属性 */
        private String userNameAttribute = "name";
    }

    /** 安全配置类 */
    @Data
    public static class Security {
        /** 最大登录尝试次数 */
        private int maxLoginAttempts = 5;

        /** 账户锁定时间 */
        private Duration lockoutDuration = Duration.ofMinutes(15);

        /** 锁定策略 */
        private LockoutStrategy lockoutStrategy = LockoutStrategy.IP_AND_USER;

        /** 是否启用设备跟踪 */
        private boolean enableDeviceTracking = true;

        /** 是否启用位置检查 */
        private boolean enableLocationCheck = false;

        /** 密码策略 */
        private Password password = new Password();
    }

    /** 密码策略配置 */
    @Data
    public static class Password {
        /** 最小长度 */
        private int minLength = 8;

        /** 最大长度 */
        private int maxLength = 128;

        /** 是否需要大写字母 */
        private boolean requireUppercase = true;

        /** 是否需要小写字母 */
        private boolean requireLowercase = true;

        /** 是否需要数字 */
        private boolean requireDigits = true;

        /** 是否需要特殊字符 */
        private boolean requireSpecialChars = true;

        /** 密码历史记录数量 */
        private int historyCount = 5;

        /** 密码过期天数 */
        private int expiryDays = 90;
    }

    /** 缓存配置类 */
    @Data
    public static class Cache {
        /** 用户信息缓存TTL（秒） */
        private long userInfoTtl = 1800;

        /** 权限缓存TTL（秒） */
        private long permissionTtl = 3600;

        /** 令牌黑名单缓存TTL（秒） */
        private long tokenBlacklistTtl = 86400;

        /** 登录失败记录缓存TTL（秒） */
        private long loginFailureTtl = 3600;
    }

    /** CORS 配置类 */
    @Data
    public static class Cors {
        /** 允许的源 */
        private String[] allowedOrigins = {"*"};

        /** 允许的方法 */
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

        /** 允许的头 */
        private String[] allowedHeaders = {"*"};

        /** 是否允许凭证 */
        private boolean allowCredentials = true;

        /** 预检请求缓存时间 */
        private long maxAge = 3600;
    }

    /** 锁定策略枚举 */
    public enum LockoutStrategy {
        /** 仅基于IP */
        IP,

        /** 仅基于用户 */
        USER,

        /** 基于IP和用户 */
        IP_AND_USER
    }
}
