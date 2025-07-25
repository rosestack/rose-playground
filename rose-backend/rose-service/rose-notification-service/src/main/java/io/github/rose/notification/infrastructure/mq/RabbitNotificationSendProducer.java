package io.github.rose.notification.infrastructure.mq;

import io.github.rose.notice.SendRequest;
import io.github.rose.notification.application.handler.NotificationSendProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 通知发送消息生产者
 */
@Component
@RequiredArgsConstructor
public class RabbitNotificationSendProducer implements NotificationSendProducer {
    private final RabbitTemplate rabbitTemplate;
    public static final String QUEUE = "notification.send.queue";

    public void send(SendRequest sendRequest) {
        rabbitTemplate.convertAndSend(QUEUE, sendRequest);
    }
}
