package io.github.rosestack.notification.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知信息 DTO
 * <p>
 * 用于向客户端返回通知信息。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Data
public class NotificationDTO {
    
    /** 通知ID */
    private String id;
    
    /** 租户ID */
    private String tenantId;
    
    /** 通道ID */
    private String channelId;
    
    /** 模板ID */
    private String templateId;
    
    /** 通知目标 */
    private String target;
    
    /** 目标类型 */
    private String targetType;
    
    /** 通知内容 */
    private String content;
    
    /** 通道类型 */
    private String channelType;
    
    /** 请求ID */
    private String requestId;
    
    /** 通知状态 */
    private String status;
    
    /** 失败原因 */
    private String failReason;
    
    /** 发送时间 */
    private LocalDateTime sendTime;
    
    /** 阅读时间 */
    private LocalDateTime readTime;
    
    /** 撤回时间 */
    private LocalDateTime recallTime;
    
    /** 追踪ID */
    private String traceId;
}