package io.github.rosestack.notification.infrastructure.mq;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notification.application.handler.NotificationSendProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** 通知发送消息生产者 */
@Component
@RequiredArgsConstructor
public class RabbitNotificationSendProducer implements NotificationSendProducer {
    private final RabbitTemplate rabbitTemplate;
    public static final String QUEUE = "notification.send.queue";

    public void send(SendRequest sendRequest) {
        rabbitTemplate.convertAndSend(QUEUE, sendRequest);
    }
}
