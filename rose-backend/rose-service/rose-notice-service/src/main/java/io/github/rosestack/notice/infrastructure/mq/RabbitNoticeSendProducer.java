package io.github.rosestack.notice.infrastructure.mq;

import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notice.application.handler.NoticeSendProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 通知发送消息生产者
 */
@Component
@RequiredArgsConstructor
public class RabbitNoticeSendProducer implements NoticeSendProducer {
    public static final String QUEUE = "notice.send.queue";
    private final RabbitTemplate rabbitTemplate;

    public void send(SendRequest sendRequest) {
        rabbitTemplate.convertAndSend(QUEUE, sendRequest);
    }
}
