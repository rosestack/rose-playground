package io.github.rosestack.notification.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

/**
 * 发送通知请求 DTO
 *
 * <p>用于接收客户端发送通知的请求参数。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Data
public class SendNotificationRequest {

    /**
     * 幂等请求ID
     */
    @NotBlank(message = "请求ID不能为空") private String requestId;

    /**
     * 通知目标
     */
    @NotBlank(message = "通知目标不能为空") private String target;

    /**
     * 目标类型
     */
    @NotNull(message = "目标类型不能为空") private String targetType;

    /**
     * 模板ID
     */
    @NotBlank(message = "模板ID不能为空") private String templateId;

    /**
     * 模板变量
     */
    private Map<String, Object> variables;
}
