package io.github.rosestack.core;

import org.springframework.core.Ordered;

/**
 * Web 常量
 * <p>
 * 复用 Spring 框架的常量，避免重复定义
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public final class Constants {

    private Constants() {
        // 工具类，禁止实例化
    }

    public static final class HeaderName {
        public static final String HEADER_TENANT_ID = "X-Tenant-ID";
        public static final String HEADER_TRACE_ID = "X-Trace-ID";
        public static final String HEADER_USER_ID = "X-User-ID";
    }

    public static final class MdcName {
        public static final String MDC_TENANT_ID = "tenantId";
        public static final String MDC_REQUEST_ID = "traceId";
        public static final String MDC_USER_ID = "userId";
    }

    public static final class FilterOrder {
        public static final int CORS_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE;
        public static final int CACHING_REQUEST_FILTER_ORDER = CORS_FILTER_ORDER + 1;
        public static final int REQUEST_FILTER_ORDER = CORS_FILTER_ORDER + 2;
        public static final int XSS_FILTER_ORDER = CORS_FILTER_ORDER + 3;
        public static final int TENANT_ID_FILTER_ORDER = CORS_FILTER_ORDER + 5;
        // Spring Security Filter 默认为 -100，可见 org.springframework.boot.autoconfigure.security.SecurityProperties
        public static final int TENANT_SECURITY_FILTER_ORDER = -99;

    }

    /**
     * 操作类型
     */
    public static final class OperationTypes {
        public static final String CREATE = "CREATE";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String EXPORT = "EXPORT";
        public static final String IMPORT = "IMPORT";
        public static final String UPLOAD = "UPLOAD";
        public static final String DOWNLOAD = "DOWNLOAD";
    }
}