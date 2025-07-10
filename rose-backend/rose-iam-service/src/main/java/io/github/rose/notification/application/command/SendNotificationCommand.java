package io.github.rose.notification.application.command;

import io.github.rose.notification.domain.value.TargetType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
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
