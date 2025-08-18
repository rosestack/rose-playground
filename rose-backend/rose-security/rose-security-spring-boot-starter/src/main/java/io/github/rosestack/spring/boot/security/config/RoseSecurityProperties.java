package io.github.rosestack.spring.boot.security.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * 安全配置属性（最小可用集）。
 */
@ConfigurationProperties(prefix = "rose.security")
@Data
public class RoseSecurityProperties {

    /** 是否启用安全特性，默认启用 */
    private boolean enabled = true;

    /** 受保护的基础路径模式，默认 /api/** */
    private String basePath = "/api/**";

    /** 登录路径，默认 /api/auth/login */
    private String loginPath = "/api/auth/login";

    /** 登出路径，默认 /api/auth/logout */
    private String logoutPath = "/api/auth/logout";

    /** 放行路径列表（支持通配符） */
    private String[] permitAll = new String[]{};

    /** Token 相关配置 */
    private final Token token = new Token();

    /** 保护（防护）相关配置 */
    private final Protect protect = new Protect();

    /** 账号安全配置 */
    private final Account account = new Account();

    @Getter
    @Setter
    public static class Token {
        /** Token 类型：LOCAL 或 JWT（默认 LOCAL） */
        private String type = "LOCAL";

        /** 读取的 Header 名称（默认 X-Auth-Token） */
        private String header = "X-Auth-Token";

        /** 过期时间（默认 PT2H） */
        private Duration ttl = Duration.ofHours(2);

        /** 存储方式：MEMORY 或 REDIS（默认 MEMORY） */
        private String store = "MEMORY";

        /** Redis Key 前缀（默认 rose:sec:token:） */
        private String redisKeyPrefix = "rose:sec:token:";
    }

    @Getter
    public static class Protect {
        private final AccessList accessList = new AccessList();
        private final RateLimit rateLimit = new RateLimit();
        private final Replay replay = new Replay();

        @Getter
        @Setter
        public static class AccessList {
            /** 是否启用访问名单（默认 false） */
            private boolean enabled = false;

            /** 组合策略：ANY/ALL（默认 ANY） */
            private String combine = "ANY";

            /** 存储方式：MEMORY/REDIS（默认 MEMORY） */
            private String store = "MEMORY";

            /** Redis Key 前缀（默认 rose:sec:access-list:） */
            private String redisKeyPrefix = "rose:sec:access-list:";

            /** 本地缓存是否启用（默认 true） */
            private boolean cacheEnabled = true;

            /** 本地缓存 TTL（默认 PT5M） */
            private Duration cacheTtl = Duration.ofMinutes(5);

            /** 动态同步刷新间隔（默认 PT1M） */
            private Duration refreshInterval = Duration.ofMinutes(1);

            /** 启用的维度（简化：ip,username） */
            private List<String> dimensions = List.of("ip", "username");
        }

        @Getter
        @Setter
        public static class RateLimit {
            /** 是否启用限流（默认 false） */
            private boolean enabled = false;
            /** 每窗口最大请求数（默认 100） */
            private int limit = 100;
            /** 窗口大小（默认 PT1M） */
            private Duration window = Duration.ofMinutes(1);
        }

        @Getter
        @Setter
        public static class Replay {
            /** 是否启用防重放（默认 false） */
            private boolean enabled = false;
            /** 时间窗（默认 PT5M） */
            private Duration window = Duration.ofMinutes(5);
            /** Nonce 头名称（默认 X-Nonce） */
            private String nonceHeader = "X-Nonce";
            /** 时间戳头名称（默认 X-Timestamp，epoch seconds） */
            private String timestampHeader = "X-Timestamp";
        }
    }

    @Getter
    public static class Account {
        private final LoginLock loginLock = new LoginLock();
        private final Kickout kickout = new Kickout();

        @Getter
        @Setter
        public static class LoginLock {
            /** 是否启用登录失败锁定（默认 false） */
            private boolean enabled = false;
            /** 最大失败次数（默认 5） */
            private int maxFailures = 5;
            /** 冷却时间（默认 PT15M） */
            private Duration cooldown = Duration.ofMinutes(15);
        }

        @Getter
        @Setter
        public static class Kickout {
            /** 是否启用主动踢人（默认 false） */
            private boolean enabled = false;

            /** 单用户并发 Token 数（默认 1） */
            private int concurrentLimit = 1;

            /** 超限时是否踢出最早 Token（默认 true） */
            private boolean kickoutOldest = true;
        }
    }
}
