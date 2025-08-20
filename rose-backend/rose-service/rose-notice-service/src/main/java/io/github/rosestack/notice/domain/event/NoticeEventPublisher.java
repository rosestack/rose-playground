package io.github.rosestack.notice.domain.event;

import org.springframework.context.ApplicationEvent;

/**
 * 通知领域事件发布者接口
 *
 * <p>定义领域事件发布的契约，用于在领域层发布事件。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public interface NoticeEventPublisher {

    /**
     * 发布应用事件
     *
     * @param applicationEvent 要发布的应用事件
     */
    void publishEvent(ApplicationEvent applicationEvent);
}
