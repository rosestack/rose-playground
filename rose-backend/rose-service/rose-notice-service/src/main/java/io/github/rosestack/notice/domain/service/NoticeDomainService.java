package io.github.rosestack.notice.domain.service;

import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.domain.entity.NoticeChannel;
import io.github.rosestack.notice.domain.entity.NoticeTemplate;
import io.github.rosestack.notice.domain.repository.NoticeChannelRepository;
import io.github.rosestack.notice.domain.repository.NoticeTemplateRepository;
import io.github.rosestack.notice.shared.constant.NoticeConstants;
import io.github.rosestack.notice.shared.exception.NoticeException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 通知领域服务
 *
 * <p>处理通知相关的核心业务逻辑，包括通知内容生成、渲染等。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class NoticeDomainService {

    /**
     * 通知模板仓储
     */
    private final NoticeTemplateRepository noticeTemplateRepository;

    /**
     * 通知通道仓储
     */
    private final NoticeChannelRepository noticeChannelRepository;

    /**
     * 渲染通知内容
     *
     * @param templateId 模板ID
     * @param parameters 参数
     * @param lang       语言
     * @return 渲染后的内容
     */
    public String renderNoticeContent(String templateId, Map<String, Object> parameters, String lang) {
        NoticeTemplate template = noticeTemplateRepository
                .findByIdAndLang(templateId, lang)
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
     *
     * <p>验证通知发送所需的参数是否有效。
     *
     * @param notice 通知对象
     * @throws NoticeException 当参数无效时抛出异常
     */
    public void validateNotice(Notice notice) {
        if (notice.getTemplateId() == null
                || notice.getTemplateId().trim().isEmpty()) {
            throw new NoticeException(NoticeConstants.ErrorCode.TEMPLATE_NOT_FOUND);
        }

        if (notice.getTarget() == null || notice.getTarget().trim().isEmpty()) {
            throw new NoticeException(NoticeConstants.ErrorCode.SEND_FAILED);
        }

        if (notice.getTargetType() == null) {
            throw new NoticeException(NoticeConstants.ErrorCode.SEND_FAILED);
        }
    }

    /**
     * 选择最佳通道
     *
     * <p>根据目标类型和用户偏好选择最佳的通知通道。
     *
     * @param targetType 目标类型
     * @param tenantId   租户ID
     * @return 选择的通道
     * @throws NoticeException 当没有可用通道时抛出异常
     */
    public NoticeChannel selectBestChannel(String targetType, String tenantId) {
        // TODO: 实现通道选择逻辑
        // 这里可以根据用户偏好、通道可用性、成本等因素选择最佳通道

        return noticeChannelRepository.findByTypeAndTenantId(targetType, tenantId).stream()
                .findFirst()
                .orElseThrow(() -> new NoticeException(NoticeConstants.ErrorCode.CHANNEL_NOT_FOUND));
    }
}
