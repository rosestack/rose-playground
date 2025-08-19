package io.github.rosestack.notification.interfaces.assembler;

import io.github.rosestack.notification.application.command.SendNotificationCommand;
import io.github.rosestack.notification.domain.entity.Notification;
import io.github.rosestack.notification.domain.value.TargetType;
import io.github.rosestack.notification.interfaces.dto.NotificationDTO;
import io.github.rosestack.notification.interfaces.dto.SendNotificationRequest;
import org.springframework.stereotype.Component;

/**
 * 通知装配器
 *
 * <p>负责在接口层 DTO 与应用层命令/领域模型之间进行转换。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Component
public class NotificationAssembler {

    /**
     * 将发送通知请求转换为发送通知命令
     *
     * @param request 发送通知请求
     * @return 发送通知命令
     */
    public SendNotificationCommand toCommand(SendNotificationRequest request) {
        return SendNotificationCommand.builder()
                .requestId(request.getRequestId())
                .target(request.getTarget())
                .targetType(TargetType.valueOf(request.getTargetType()))
                .templateId(request.getTemplateId())
                .variables(request.getVariables())
                .build();
    }

    /**
     * 将通知领域模型转换为通知 DTO
     *
     * @param notification 通知领域模型
     * @return 通知 DTO
     */
    public NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTenantId(notification.getTenantId());
        dto.setChannelId(notification.getChannelId());
        dto.setTemplateId(notification.getTemplateId());
        dto.setTarget(notification.getTarget());
        dto.setTargetType(
                notification.getTargetType() != null
                        ? notification.getTargetType().name()
                        : null);
        dto.setContent(notification.getContent());
        dto.setChannelType(
                notification.getChannelType() != null
                        ? notification.getChannelType().name()
                        : null);
        dto.setRequestId(notification.getRequestId());
        dto.setStatus(
                notification.getStatus() != null ? notification.getStatus().name() : null);
        dto.setFailReason(notification.getFailReason());
        dto.setSendTime(notification.getSendTime());
        dto.setReadTime(notification.getReadTime());
        dto.setRecallTime(notification.getRecallTime());
        dto.setTraceId(notification.getTraceId());
        return dto;
    }
}
