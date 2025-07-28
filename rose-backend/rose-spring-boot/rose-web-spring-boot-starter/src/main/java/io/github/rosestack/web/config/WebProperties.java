package io.github.rosestack.web.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

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
    private CorsConfiguration cors = new CorsConfiguration();

    /**
     * Jackson 配置
     */
    private JacksonProperties jackson = new JacksonProperties();

    /**
     * 过滤器配置
     */
    private Filter filter = new Filter();

    /**
     * 异常处理配置
     */
    private Exception exception = new Exception();

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

            private String mdcName = "requestId";
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
}