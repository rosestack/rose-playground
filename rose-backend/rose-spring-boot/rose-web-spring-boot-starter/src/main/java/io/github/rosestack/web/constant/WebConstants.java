package io.github.rosestack.web.constant;

/**
 * Web 常量
 * <p>
 * 复用 Spring 框架的常量，避免重复定义
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public final class WebConstants {

    private WebConstants() {
        // 工具类，禁止实例化
    }

    /**
     * 时间格式
     */
    public static final class DateFormats {
        public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
        public static final String DATE = "yyyy-MM-dd";
        public static final String TIME = "HH:mm:ss";
        public static final String DATE_TIME_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        public static final String DATE_TIME_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
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