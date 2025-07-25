package io.github.rosestack.notification.application.service;

import io.github.rosestack.notice.NoticeService;
import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.SendResult;
import io.github.rosestack.notice.SenderConfiguration;
import io.github.rosestack.notification.application.command.SendNotificationCommand;
import io.github.rosestack.notification.domain.entity.Notification;
import io.github.rosestack.notification.domain.entity.NotificationChannel;
import io.github.rosestack.notification.domain.entity.NotificationTemplate;
import io.github.rosestack.notification.domain.entity.NotificationTemplateChannel;
import io.github.rosestack.notification.domain.repository.NotificationChannelRepository;
import io.github.rosestack.notification.domain.repository.NotificationRepository;
import io.github.rosestack.notification.domain.repository.NotificationTemplateChannelRepository;
import io.github.rosestack.notification.domain.repository.NotificationTemplateRepository;
import io.github.rosestack.notification.shared.constant.NotificationConstants;
import io.github.rosestack.notification.shared.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知应用服务（Application Service）
 * <p>
 * 负责通知发送用例的编排，包括参数校验、模板和渠道查找、组装发送上下文、调用发送服务等。
 * 不直接处理发送动作、事件、重试、日志等横切逻辑，这些由 NotificationSendService 负责。
 * <p>
 * 典型职责：
 * <ul>
 *   <li>参数和幂等校验</li>
 *   <li>查找通知模板和渠道</li>
 *   <li>组装 NotificationSendContext</li>
 *   <li>调用 NotificationSendService 完成实际发送</li>
 *   <li>只暴露用例入口方法，内部细节私有化</li>
 * </ul>
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 */
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateChannelRepository notificationTemplateChannelRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final NoticeService noticeService;

    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(SendNotificationCommand cmd) {
        NotificationTemplate template = templateRepository.findByIdAndLang(cmd.getTemplateId(), LocaleContextHolder.getLocale().getLanguage())
                .orElseThrow(() -> {
                    log.warn("模板不存在，templateId={}, lang={}", cmd.getTemplateId(), LocaleContextHolder.getLocale().getLanguage());
                    throw new NotificationException(NotificationConstants.ErrorCode.TEMPLATE_NOT_FOUND);
                });

        List<NotificationTemplateChannel> notificationTemplateChannels = notificationTemplateChannelRepository.findByTemplateId(cmd.getTemplateId());
        if (notificationTemplateChannels == null || notificationTemplateChannels.isEmpty()) {
            log.warn("模板未配置任何渠道，templateId={}", cmd.getTemplateId());
            throw new NotificationException(NotificationConstants.ErrorCode.CHANNEL_NOT_FOUND);
        }

        for (NotificationTemplateChannel notificationTemplateChannel : notificationTemplateChannels) {
            String channelId = notificationTemplateChannel.getChannelId();
            NotificationChannel channel = notificationChannelRepository.findById(channelId)
                    .orElseThrow(() -> {
                        log.warn("渠道不存在，channelId={}", channelId);
                        throw new NotificationException(NotificationConstants.ErrorCode.CHANNEL_NOT_FOUND);
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
            SendResult sendResult = noticeService.send(sendRequest,configuration);

            Notification notification = new Notification();
            notification.setChannelId(channelId);
            notification.setTemplateId(cmd.getTemplateId());
            notification.setTarget(cmd.getTarget());
            notification.setChannelType(channel.getChannelType());
            notification.setSendTime(LocalDateTime.now());
            notification.setRequestId(sendRequest.getRequestId());
            notification.setFailReason(sendResult.getMessage());
            notification.setTraceId(MDC.get("traceId"));

            notificationRepository.save(notification);
        }
    }
}
