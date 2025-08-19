package io.github.rosestack.notification.domain.event.listener;

import io.github.rosestack.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final NotificationRepository notificationRepository;
}
