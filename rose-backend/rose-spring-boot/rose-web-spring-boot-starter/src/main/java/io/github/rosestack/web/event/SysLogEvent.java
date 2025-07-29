package io.github.rosestack.web.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统日志事件
 * <p>
 * 封装系统操作日志信息，用于事件发布和处理
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
public class SysLogEvent {

    /**
     * 业务模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String name;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 返回结果
     */
    private String result;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 地理位置
     */
    private String location;
}
