package io.github.rosestack.spring.boot.security.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置属性（最小可用集）。
 */
@ConfigurationProperties(prefix = "rose.security")
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
    private List<String> permitAll = new ArrayList<>();

    /** Token 相关配置 */
    private final Token token = new Token();

    /** 保护（防护）相关配置 */
    private final Protect protect = new Protect();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public String getLogoutPath() {
        return logoutPath;
    }

    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }

    public List<String> getPermitAll() {
        return permitAll;
    }

    public void setPermitAll(List<String> permitAll) {
        this.permitAll = permitAll;
    }

    public Token getToken() {
        return token;
    }

    public Protect getProtect() {
        return protect;
    }

    // =============================
    // 嵌套属性对象
    // =============================

    public static class Token {
        /** 是否启用 Token 功能（默认启用） */
        private boolean enabled = true;

        /** Token 类型：LOCAL 或 JWT（默认 LOCAL） */
        private String type = "LOCAL";

        /** 读取的 Header 名称（默认 X-Auth-Token） */
        private String header = "X-Auth-Token";

        /** 过期时间（默认 PT2H） */
        private Duration ttl = Duration.ofHours(2);

        /** 单用户并发 Token 数（默认 1） */
        private int concurrentLimit = 1;

        /** 超限时是否踢出最早 Token（默认 true） */
        private boolean kickoutOldest = true;

        /** 存储方式：MEMORY 或 REDIS（默认 MEMORY） */
        private String store = "MEMORY";

        /** Redis Key 前缀（默认 rose:sec:token:） */
        private String redisKeyPrefix = "rose:sec:token:";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public int getConcurrentLimit() {
            return concurrentLimit;
        }

        public void setConcurrentLimit(int concurrentLimit) {
            this.concurrentLimit = concurrentLimit;
        }

        public boolean isKickoutOldest() {
            return kickoutOldest;
        }

        public void setKickoutOldest(boolean kickoutOldest) {
            this.kickoutOldest = kickoutOldest;
        }

        public String getStore() {
            return store;
        }

        public void setStore(String store) {
            this.store = store;
        }

        public String getRedisKeyPrefix() {
            return redisKeyPrefix;
        }

        public void setRedisKeyPrefix(String redisKeyPrefix) {
            this.redisKeyPrefix = redisKeyPrefix;
        }
    }

    public static class Protect {
        private final AccessList accessList = new AccessList();

        public AccessList getAccessList() {
            return accessList;
        }

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

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getCombine() {
                return combine;
            }

            public void setCombine(String combine) {
                this.combine = combine;
            }

            public String getStore() {
                return store;
            }

            public void setStore(String store) {
                this.store = store;
            }

            public String getRedisKeyPrefix() {
                return redisKeyPrefix;
            }

            public void setRedisKeyPrefix(String redisKeyPrefix) {
                this.redisKeyPrefix = redisKeyPrefix;
            }

            public boolean isCacheEnabled() {
                return cacheEnabled;
            }

            public void setCacheEnabled(boolean cacheEnabled) {
                this.cacheEnabled = cacheEnabled;
            }

            public Duration getCacheTtl() {
                return cacheTtl;
            }

            public void setCacheTtl(Duration cacheTtl) {
                this.cacheTtl = cacheTtl;
            }

            public Duration getRefreshInterval() {
                return refreshInterval;
            }

            public void setRefreshInterval(Duration refreshInterval) {
                this.refreshInterval = refreshInterval;
            }

            public List<String> getDimensions() {
                return dimensions;
            }

            public void setDimensions(List<String> dimensions) {
                this.dimensions = dimensions;
            }
        }
    }
}


