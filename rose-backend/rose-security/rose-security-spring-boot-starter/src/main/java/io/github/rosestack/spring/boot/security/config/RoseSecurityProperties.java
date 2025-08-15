package io.github.rosestack.spring.boot.security.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rose Security 配置属性
 *
 * <p>提供认证与授权相关的配置选项
 *
 * @author rosestack
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.security")
public class RoseSecurityProperties {

    /**
     * 是否启用 Security 自动配置
     */
    private boolean enabled = true;

    private boolean stateless = true;

    /**
     * 基础认证配置
     */
    private Auth auth = new Auth();

    /**
     * JWT 配置
     */
    private Jwt jwt = new Jwt();

    /**
     * OAuth2 配置
     */
    private OAuth2 oauth2 = new OAuth2();

    /**
     * MFA 配置
     */
    private Mfa mfa = new Mfa();

    /**
     * 安全防护配置
     */
    private Protection protection = new Protection();

    /**
     * 可观测性配置
     */
    private Observability observability = new Observability();

    /**
     * 基础认证配置
     */
    @Data
    public static class Auth {
        /**
         * 是否启用基础认证
         */
        private boolean enabled = true;

        /**
         * 登录端点路径
         */
        private String loginPath = "/api/auth/login";

        /**
         * 注销端点路径
         */
        private String logoutPath = "/api/auth/logout";

        /**
         * 刷新端点路径
         */
        private String refreshPath = "/api/auth/refresh";

        /**
         * 受保护路径
         */
        private String bashPath = "/api/**";

        /**
         * 允许访问的路径
         */
        private String[] permitPaths = new String[] {};

        /**
         * Token 配置
         */
        private Token token = new Token();

        /**
         * 账号安全配置
         */
        private Account account = new Account();

        /**
         * Token 配置
         */
        @Data
        public static class Token {
            /**
             * AccessToken 有效期
             */
            private Duration accessTokenExpiredTime = Duration.ofHours(24);

            /**
             * RefreshToken 有效期
             */
            private Duration refreshTokenExpiredTime = Duration.ofMinutes(30);

            /**
             * Token 刷新时间窗口
             */
            private Duration refreshWindow = Duration.ofMinutes(5);

            /**
             * 最大并发会话数
             */
            private int maximumSessions = 1;

            /**
             * 当达到最大会话数时是否阻止新登录
             */
            private boolean maxSessionsPreventsLogin = false;

            /**
             * Token 存储类型
             */
            private StorageType storageType = StorageType.MEMORY;

            /**
             * Redis 相关配置
             */
            private Redis redis = new Redis();

            /**
             * Token 存储类型枚举
             */
            public enum StorageType {
                MEMORY,
                REDIS
            }

            /**
             * Redis 配置
             */
            @Data
            public static class Redis {
                /**
                 * Redis Key 前缀
                 */
                private String keyPrefix = "rose:security:token:";

                /**
                 * 数据库索引
                 */
                private int database = 0;
            }
        }

        /**
         * 账号安全配置
         */
        @Data
        public static class Account {
            /**
             * 密码策略配置
             */
            private Password password = new Password();

            /**
             * 登录失败锁定配置
             */
            private LockOut lockOut = new LockOut();

            /**
             * 验证码配置
             */
            private Captcha captcha = new Captcha();

            /**
             * 密码策略配置
             */
            @Data
            public static class Password {
                /**
                 * 最小长度
                 */
                private int minLength = 8;

                /**
                 * 密码历史数量
                 */
                private int history = 5;

                /**
                 * 密码过期天数
                 */
                private int expireDays = 90;

                /**
                 * 是否需要大写字母
                 */
                private boolean requireUppercase = true;

                /**
                 * 是否需要小写字母
                 */
                private boolean requireLowercase = true;

                /**
                 * 是否需要数字
                 */
                private boolean requireDigit = true;

                /**
                 * 是否需要特殊字符
                 */
                private boolean requireSpecialChar = true;
            }

            /**
             * 登录失败锁定配置
             */
            @Data
            public static class LockOut {
                /**
                 * 是否启用锁定
                 */
                private boolean enabled = true;

                /**
                 * 最大失败次数
                 */
                private int maxAttempts = 5;

                /**
                 * 锁定时间（分钟）
                 */
                private Duration lockDuration = Duration.ofMinutes(30);
            }

            /**
             * 验证码配置
             */
            @Data
            public static class Captcha {
                /**
                 * 是否启用验证码
                 */
                private boolean enabled = false;

                /**
                 * 失败多少次后启用验证码
                 */
                private int enableAfterFailures = 3;
            }
        }
    }

    /**
     * JWT 配置
     */
    @Data
    public static class Jwt {
        /**
         * 是否启用 JWT
         */
        private boolean enabled = false;

        /**
         * JWT 密钥
         */
        private String secret = "rose-security-jwt-secret-key-change-in-production";

        /**
         * JWT 签名算法
         */
        private Algorithm algorithm = Algorithm.HS256;

        /**
         * JWT Token 有效期
         */
        private Duration expiration = Duration.ofHours(24);

        /**
         * 时钟偏移容错时间
         */
        private Duration clockSkew = Duration.ofMinutes(5);

        /**
         * 密钥配置
         */
        private Key key = new Key();

        /**
         * JWT 算法枚举
         */
        public enum Algorithm {
            HS256,
            HS384,
            HS512,
            RS256,
            RS384,
            RS512,
            ES256,
            ES384,
            ES512
        }

        /**
         * 密钥配置
         */
        @Data
        public static class Key {
            /**
             * 密钥存储类型
             */
            private KeyType type = KeyType.SECRET;

            /**
             * JWK Set URI
             */
            private String jwkSetUri;

            /**
             * Keystore 路径（支持 classpath:, file: 或绝对路径），默认尝试 JKS，其次 PKCS12
             */
            private String keystorePath;

            /**
             * Keystore 密码
             */
            private String keystorePassword;

            /**
             * 密钥别名
             */
            private String keyAlias;

            /**
             * 密钥轮换间隔
             */
            private Duration rotationInterval = Duration.ofDays(30);

            /**
             * JWKS 拉取连接超时（毫秒）
             */
            private int jwkConnectTimeoutMillis = 2000;

            /**
             * JWKS 拉取读取超时（毫秒）
             */
            private int jwkReadTimeoutMillis = 3000;

            /**
             * JWKS 拉取最大重试次数（含首次），最小为1
             */
            private int jwkMaxRetries = 1;

            /**
             * 拉取失败时是否回退到缓存（若存在且未超过轮换间隔）
             */
            private boolean jwkFallbackToCache = true;

            /**
             * 密钥类型枚举
             */
            public enum KeyType {
                SECRET,
                JWK,
                KEYSTORE
            }
        }

        /**
         * 可选标准声明校验与元数据
         */
        private String issuer; // iss

        private List<String> audience = new ArrayList<>(); // aud
        private boolean requireIssuedAt = true; // 是否强制要求 iat
        private boolean requireNotBefore = false; // 是否强制要求 nbf

        /**
         * 验证器创建失败时是否回退到 HS Secret 验证
         */
        private boolean fallbackToSecretForVerify = false;
    }

    /**
     * OAuth2 配置
     */
    @Data
    public static class OAuth2 {
        /**
         * 是否启用 OAuth2
         */
        private boolean enabled = false;

        /**
         * 客户端配置
         */
        private Client client = new Client();

        /**
         * OAuth2 客户端配置
         */
        @Data
        public static class Client {
            /**
             * 重定向 URI
             */
            private String redirectUri = "/login/oauth2/code/{registrationId}";

            /**
             * 登录成功跳转URI
             */
            private String successUrl = "/";

            /**
             * 登录失败跳转URI
             */
            private String failureUrl = "/login?error";
        }
    }

    /**
     * MFA 配置
     */
    @Data
    public static class Mfa {
        /**
         * 是否启用 MFA
         */
        private boolean enabled = false;

        /**
         * TOTP 配置
         */
        private Totp totp = new Totp();

        /**
         * TOTP 配置
         */
        @Data
        public static class Totp {
            /**
             * 是否启用 TOTP
             */
            private boolean enabled = true;

            /**
             * 应用名称
             */
            private String applicationName = "Rose Security";

            /**
             * 时间步长（秒）
             */
            private int timeStep = 30;

            /**
             * 代码长度
             */
            private int codeLength = 6;
        }
    }

    /**
     * 安全防护配置
     */
    @Data
    public static class Protection {
        /**
         * IP 限制配置
         */
        private Ip ip = new Ip();

        /**
         * 速率限制配置
         */
        private RateLimit rateLimit = new RateLimit();

        /**
         * 防重放配置
         */
        private AntiReplay antiReplay = new AntiReplay();

        /**
         * IP 限制配置
         */
        @Data
        public static class Ip {
            /**
             * 是否启用 IP 限制
             */
            private boolean enabled = false;

            /**
             * IP 白名单
             */
            private List<String> whitelist = new ArrayList<>();

            /**
             * IP 黑名单
             */
            private List<String> blacklist = new ArrayList<>();
        }

        /**
         * 速率限制配置
         */
        @Data
        public static class RateLimit {
            /**
             * 是否启用速率限制
             */
            private boolean enabled = false;

            /**
             * 每分钟请求数限制
             */
            private int requestsPerMinute = 60;

            /**
             * 时间窗口大小
             */
            private Duration windowSize = Duration.ofMinutes(1);
        }

        /**
         * 防重放配置
         */
        @Data
        public static class AntiReplay {
            /**
             * 是否启用防重放
             */
            private boolean enabled = false;

            /**
             * 时间窗口大小
             */
            private Duration windowSize = Duration.ofMinutes(5);
        }
    }

    /**
     * 可观测性配置
     */
    @Data
    public static class Observability {
        /**
         * 是否启用指标收集
         */
        private boolean metricsEnabled = true;

        /**
         * 是否启用结构化日志
         */
        private boolean structuredLoggingEnabled = true;

        /**
         * 是否启用链路追踪
         */
        private boolean tracingEnabled = false;
    }
}
