package io.github.rosestack.notice.application.service;

import io.github.rosestack.notify.NotifyService;
import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notify.SendResult;
import io.github.rosestack.notify.SenderConfiguration;
import io.github.rosestack.notice.application.command.SendNoticeCommand;
import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.domain.entity.NoticeChannel;
import io.github.rosestack.notice.domain.entity.NoticeTemplate;
import io.github.rosestack.notice.domain.entity.NoticeTemplateChannel;
import io.github.rosestack.notice.domain.repository.NoticeChannelRepository;
import io.github.rosestack.notice.domain.repository.NoticeRepository;
import io.github.rosestack.notice.domain.repository.NoticeTemplateChannelRepository;
import io.github.rosestack.notice.domain.repository.NoticeTemplateRepository;
import io.github.rosestack.notice.shared.constant.NoticeConstants;
import io.github.rosestack.notice.shared.exception.NoticeException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知应用服务（Application Service）
 *
 * <p>负责通知发送用例的编排，包括参数校验、模板和渠道查找、组装发送上下文、调用发送服务等。 不直接处理发送动作、事件、重试、日志等横切逻辑，这些由
 * NoticeSendService 负责。
 *
 * <p>典型职责：
 *
 * <ul>
 *   <li>参数和幂等校验
 *   <li>查找通知模板和渠道
 *   <li>组装 NoticeSendContext
 *   <li>调用 NoticeSendService 完成实际发送
 *   <li>只暴露用例入口方法，内部细节私有化
 * </ul>
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 */
@Service
@RequiredArgsConstructor
public class NoticeApplicationService {
    private static final Logger log = LoggerFactory.getLogger(NoticeApplicationService.class);

    private final NoticeRepository noticeRepository;
    private final NoticeTemplateRepository templateRepository;
    private final NoticeTemplateChannelRepository noticeTemplateChannelRepository;
    private final NoticeChannelRepository noticeChannelRepository;
    private final NotifyService notifyService;

    @Transactional(rollbackFor = Exception.class)
    public void sendNotice(SendNoticeCommand cmd) {
        NoticeTemplate template = templateRepository
                .findByIdAndLang(
                        cmd.getTemplateId(), LocaleContextHolder.getLocale().getLanguage())
                .orElseThrow(() -> {
                    log.warn(
                            "模板不存在，templateId={}, lang={}",
                            cmd.getTemplateId(),
                            LocaleContextHolder.getLocale().getLanguage());
                    throw new NoticeException(NoticeConstants.ErrorCode.TEMPLATE_NOT_FOUND);
                });

        List<NoticeTemplateChannel> noticeTemplateChannels =
                noticeTemplateChannelRepository.findByTemplateId(cmd.getTemplateId());
        if (noticeTemplateChannels == null || noticeTemplateChannels.isEmpty()) {
            log.warn("模板未配置任何渠道，templateId={}", cmd.getTemplateId());
            throw new NoticeException(NoticeConstants.ErrorCode.CHANNEL_NOT_FOUND);
        }

        for (NoticeTemplateChannel noticeTemplateChannel : noticeTemplateChannels) {
            String channelId = noticeTemplateChannel.getChannelId();
            NoticeChannel channel = noticeChannelRepository
                    .findById(channelId)
                    .orElseThrow(() -> {
                        log.warn("渠道不存在，channelId={}", channelId);
                        throw new NoticeException(NoticeConstants.ErrorCode.CHANNEL_NOT_FOUND);
                    });

            SendRequest sendRequest = SendRequest.builder()
                    .target(cmd.getTarget())
                    .requestId(cmd.getRequestId())
                    .templateContent(template.getContent())
                    .build();
            SenderConfiguration configuration = SenderConfiguration.builder()
                    .channelType(channel.getChannelType().name())
                    .config(channel.getConfig())
                    .templateType(template.getType())
                    .build();
            SendResult sendResult = notifyService.send(sendRequest, configuration);

            Notice notice = new Notice();
            notice.setChannelId(channelId);
            notice.setTemplateId(cmd.getTemplateId());
            notice.setTarget(cmd.getTarget());
            notice.setChannelType(channel.getChannelType());
            notice.setSendTime(LocalDateTime.now());
            notice.setRequestId(sendRequest.getRequestId());
            notice.setFailReason(sendResult.getMessage());
            notice.setTraceId(MDC.get("traceId"));

            noticeRepository.save(notice);
        }
    }
}
