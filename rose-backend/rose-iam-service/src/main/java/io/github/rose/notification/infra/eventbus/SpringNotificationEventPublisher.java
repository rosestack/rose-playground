package io.github.rose.notification.infra.eventbus;

import io.github.rose.notification.domain.event.listener.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SpringNotificationEventPublisher implements NotificationEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(ApplicationEvent applicationEvent) {
        publisher.publishEvent(applicationEvent);
    }
}
