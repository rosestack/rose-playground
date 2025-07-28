package io.github.rosestack.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 配置属性
 * <p>
 * 提供 Web 相关的配置选项
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "rose.web")
public class WebProperties {

    /**
     * 是否启用 Web 自动配置
     */
    private boolean enabled = true;

    /**
     * 跨域配置
     */
    private Cors cors = new Cors();

    /**
     * Jackson 配置
     */
    private Jackson jackson = new Jackson();

    /**
     * 过滤器配置
     */
    private Filter filter = new Filter();

    /**
     * 异常处理配置
     */
    private Exception exception = new Exception();

    /**
     * 跨域配置
     */
    @Data
    public static class Cors {
        /**
         * 是否启用跨域
         */
        private boolean enabled = true;

        /**
         * 允许的源
         */
        private String[] allowedOrigins = {"*"};

        /**
         * 允许的方法
         */
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

        /**
         * 允许的请求头
         */
        private String[] allowedHeaders = {"*"};

        /**
         * 是否允许携带凭证
         */
        private boolean allowCredentials = true;

        /**
         * 预检请求的有效期（秒）
         */
        private long maxAge = 3600L;
    }

    /**
     * Jackson 配置
     */
    @Data
    public static class Jackson {
        /**
         * 日期格式
         */
        private String dateFormat = "yyyy-MM-dd HH:mm:ss";

        /**
         * 时区
         */
        private String timeZone = "Asia/Shanghai";

        /**
         * 是否将日期写为时间戳
         */
        private boolean writeDatesAsTimestamps = false;

        /**
         * 空 Bean 是否失败
         */
        private boolean failOnEmptyBeans = false;

        /**
         * 未知属性是否失败
         */
        private boolean failOnUnknownProperties = false;

    }

    /**
     * 过滤器配置
     */
    @Data
    public static class Filter {
        /**
         * 请求ID过滤器
         */
        private RequestId requestId = new RequestId();

        /**
         * XSS 过滤器
         */
        private Xss xss = new Xss();

        /**
         * 请求ID过滤器配置
         */
        @Data
        public static class RequestId {
            /**
             * 是否启用
             */
            private boolean enabled = true;

            /**
             * 请求ID头名称
             */
            private String headerName = "X-Request-ID";

            /**
             * 响应头名称
             */
            private String responseHeaderName = "X-Request-ID";
        }

        /**
         * XSS 过滤器配置
         */
        @Data
        public static class Xss {
            /**
             * 是否启用
             */
            private boolean enabled = true;

            /**
             * 排除的路径
             */
            private String[] excludePaths = {"/actuator/**"};
        }
    }

    /**
     * 异常处理配置
     */
    @Data
    public static class Exception {
        /**
         * 是否启用全局异常处理
         */
        private boolean enabled = true;

        /**
         * 是否包含异常堆栈信息
         */
        private boolean includeStackTrace = false;

        /**
         * 是否记录异常日志
         */
        private boolean logException = true;
    }
} 