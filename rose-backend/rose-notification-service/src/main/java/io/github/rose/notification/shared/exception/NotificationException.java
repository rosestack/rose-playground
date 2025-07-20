package io.github.rose.notification.shared.exception;

/**
 * 通知业务异常基类
 * <p>
 * 所有通知相关的业务异常都应继承此类。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public class NotificationException extends RuntimeException {
    
    /** 错误码 */
    private final String errorCode;
    
    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public NotificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误消息
     * @param cause 原因异常
     */
    public NotificationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
}