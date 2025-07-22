package io.github.rose.infrastructure.constants;

/**
 * 响应头常量定义
 * 用于传递额外的错误处理信息
 */
public class HeaderConstants {
    /** 请求追踪ID */
    public static final String REQUEST_ID = "X-Request-ID";
    
    /** 响应时间戳 */
    public static final String RESPONSE_TIME = "X-Response-Time";
    
    /** 是否可重试 */
    public static final String RETRY_ALLOWED = "X-Retry-Allowed";
    
    /** 重试间隔（标准头） */
    public static final String RETRY_AFTER = "Retry-After";
    
    /** 错误详情 */
    public static final String ERROR_DETAILS = "X-Error-Details";
}