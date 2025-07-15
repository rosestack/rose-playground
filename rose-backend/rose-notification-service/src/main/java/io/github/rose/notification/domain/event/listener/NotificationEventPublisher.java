package io.github.rose.notification.domain.event.listener;

import org.springframework.context.ApplicationEvent;

public interface NotificationEventPublisher {
    void publishEvent(ApplicationEvent applicationEvent);
}
