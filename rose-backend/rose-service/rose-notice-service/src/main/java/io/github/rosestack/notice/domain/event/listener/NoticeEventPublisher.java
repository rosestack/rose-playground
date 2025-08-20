package io.github.rosestack.notice.domain.event.listener;

import org.springframework.context.ApplicationEvent;

public interface NoticeEventPublisher {
    void publishEvent(ApplicationEvent applicationEvent);
}
