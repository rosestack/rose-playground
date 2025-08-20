package io.github.rosestack.notice.infrastructure.mq;

import io.github.rosestack.notify.NotifyService;
import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notice.application.handler.NoticeSendConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知发送消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitNoticeSendConsumer implements NoticeSendConsumer {
    private final NotifyService notifyService;

    @RabbitListener(queues = RabbitNoticeSendProducer.QUEUE)
    public void consume(SendRequest sendRequest) {
        try {
            notifyService.send(sendRequest, null);
        } catch (Exception e) {
            log.error("[通知异步发送] 失败, requestId={},error={}", sendRequest.getRequestId(), e.getMessage(), e);
            // 可扩展：失败重试、死信队列等
        }
    }
}
