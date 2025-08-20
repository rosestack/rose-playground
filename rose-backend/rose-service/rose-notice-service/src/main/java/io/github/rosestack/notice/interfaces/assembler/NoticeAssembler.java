package io.github.rosestack.notice.interfaces.assembler;

import io.github.rosestack.notice.application.command.SendNoticeCommand;
import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.domain.value.TargetType;
import io.github.rosestack.notice.interfaces.dto.NoticeDTO;
import io.github.rosestack.notice.interfaces.dto.SendNoticeRequest;
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
public class NoticeAssembler {

    /**
     * 将发送通知请求转换为发送通知命令
     *
     * @param request 发送通知请求
     * @return 发送通知命令
     */
    public SendNoticeCommand toCommand(SendNoticeRequest request) {
        return SendNoticeCommand.builder()
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
     * @param notice 通知领域模型
     * @return 通知 DTO
     */
    public NoticeDTO toDTO(Notice notice) {
        NoticeDTO dto = new NoticeDTO();
        dto.setId(notice.getId());
        dto.setTenantId(notice.getTenantId());
        dto.setChannelId(notice.getChannelId());
        dto.setTemplateId(notice.getTemplateId());
        dto.setTarget(notice.getTarget());
        dto.setTargetType(
                notice.getTargetType() != null
                        ? notice.getTargetType().name()
                        : null);
        dto.setContent(notice.getContent());
        dto.setChannelType(
                notice.getChannelType() != null
                        ? notice.getChannelType().name()
                        : null);
        dto.setRequestId(notice.getRequestId());
        dto.setStatus(
                notice.getStatus() != null ? notice.getStatus().name() : null);
        dto.setFailReason(notice.getFailReason());
        dto.setSendTime(notice.getSendTime());
        dto.setReadTime(notice.getReadTime());
        dto.setRecallTime(notice.getRecallTime());
        dto.setTraceId(notice.getTraceId());
        return dto;
    }
}
