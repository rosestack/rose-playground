package io.github.rosestack.notification.shared.constant;

/**
 * 通知常量定义
 *
 * <p>定义通知服务中使用的常量。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public final class NotificationConstants {

    /** 私有构造函数，防止实例化 */
    private NotificationConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    /** 默认租户ID */
    public static final String DEFAULT_TENANT_ID = "default";

    /** 最大重试次数 */
    public static final int MAX_RETRY_COUNT = 3;

    /** 默认超时时间（毫秒） */
    public static final long DEFAULT_TIMEOUT_MS = 30000L;

    /** 消息队列相关常量 */
    public static final class Queue {
        /** 通知发送队列名称 */
        public static final String NOTIFICATION_SEND_QUEUE = "notification.send.queue";

        /** 私有构造函数 */
        private Queue() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    /** 错误码常量 */
    public static final class ErrorCode {
        /** 通知不存在 */
        public static final String NOTIFICATION_NOT_FOUND = "NOTIFICATION_NOT_FOUND";

        /** 模板不存在 */
        public static final String TEMPLATE_NOT_FOUND = "TEMPLATE_NOT_FOUND";

        /** 通道不存在 */
        public static final String CHANNEL_NOT_FOUND = "CHANNEL_NOT_FOUND";

        /** 发送失败 */
        public static final String SEND_FAILED = "SEND_FAILED";

        /** 私有构造函数 */
        private ErrorCode() {
            throw new UnsupportedOperationException("Utility class");
        }
    }
}
