package io.github.rose.notification.domain.service;

import io.github.rose.notification.domain.entity.Notification;
import io.github.rose.notification.domain.entity.NotificationChannel;
import io.github.rose.notification.domain.entity.NotificationTemplate;
import io.github.rose.notification.domain.repository.NotificationChannelRepository;
import io.github.rose.notification.domain.repository.NotificationTemplateRepository;
import io.github.rose.notification.shared.constant.NotificationConstants;
import io.github.rose.notification.shared.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通知领域服务
 * <p>
 * 处理通知相关的核心业务逻辑，包括通知内容生成、渲染等。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    /**
     * 通知模板仓储
     */
    private final NotificationTemplateRepository notificationTemplateRepository;

    /**
     * 通知通道仓储
     */
    private final NotificationChannelRepository notificationChannelRepository;

    /**
     * 渲染通知内容
     *
     * @param templateId 模板ID
     * @param parameters 参数
     * @param lang       语言
     * @return 渲染后的内容
     */
    public String renderNotificationContent(String templateId, Map<String, Object> parameters, String lang) {
        NotificationTemplate template = notificationTemplateRepository.findByIdAndLang(templateId, lang)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));

        // 简单的模板渲染逻辑
        String content = template.getContent();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }

        return content;
    }

    /**
     * 验证通知参数
     * <p>
     * 验证通知发送所需的参数是否有效。
     *
     * @param notification 通知对象
     * @throws NotificationException 当参数无效时抛出异常
     */
    public void validateNotification(Notification notification) {
        if (notification.getTemplateId() == null || notification.getTemplateId().trim().isEmpty()) {
            throw new NotificationException(NotificationConstants.ErrorCode.TEMPLATE_NOT_FOUND);
        }

        if (notification.getTarget() == null || notification.getTarget().trim().isEmpty()) {
            throw new NotificationException(NotificationConstants.ErrorCode.SEND_FAILED);
        }

        if (notification.getTargetType() == null) {
            throw new NotificationException(NotificationConstants.ErrorCode.SEND_FAILED);
        }
    }

    /**
     * 选择最佳通道
     * <p>
     * 根据目标类型和用户偏好选择最佳的通知通道。
     *
     * @param targetType 目标类型
     * @param tenantId   租户ID
     * @return 选择的通道
     * @throws NotificationException 当没有可用通道时抛出异常
     */
    public NotificationChannel selectBestChannel(String targetType, String tenantId) {
        // TODO: 实现通道选择逻辑
        // 这里可以根据用户偏好、通道可用性、成本等因素选择最佳通道

        return notificationChannelRepository.findByTypeAndTenantId(targetType, tenantId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotificationException(NotificationConstants.ErrorCode.CHANNEL_NOT_FOUND));
    }
}