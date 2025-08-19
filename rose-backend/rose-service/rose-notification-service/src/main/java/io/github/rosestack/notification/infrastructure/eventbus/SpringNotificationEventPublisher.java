package io.github.rosestack.notification.infrastructure.eventbus;

import io.github.rosestack.notification.domain.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring 通知事件发布者实现
 *
 * <p>基于 Spring 事件机制实现的领域事件发布者。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SpringNotificationEventPublisher implements NotificationEventPublisher {

	/**
	 * Spring 应用事件发布器
	 */
	private final ApplicationEventPublisher publisher;

	@Override
	public void publishEvent(ApplicationEvent applicationEvent) {
		publisher.publishEvent(applicationEvent);
	}
}
