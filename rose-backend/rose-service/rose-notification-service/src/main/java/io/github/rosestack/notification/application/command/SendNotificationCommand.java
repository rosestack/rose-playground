package io.github.rosestack.notification.application.command;

import io.github.rosestack.notification.domain.value.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class SendNotificationCommand {
    /**
     * 幂等ID/请求ID，强烈建议由调用方生成（如UUID）
     */
    private String requestId;

    /**
     * 通知目标（如邮箱、手机号、用户名）
     */
    @NotBlank
    private String target;

    /**
     * 目标类型（如 EMAIL、MOBILE、USERNAME、USERID）
     */
    @NotNull
    private TargetType targetType;

    /**
     * 模板ID，驱动一切
     */
    @NotBlank
    private String templateId;

    /**
     * 模板变量
     */
    private Map<String, Object> variables = new HashMap<>();
}
