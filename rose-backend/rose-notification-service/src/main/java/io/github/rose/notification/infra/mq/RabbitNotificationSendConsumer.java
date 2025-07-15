package io.github.rose.notification.infra.mq;

import io.github.rose.notice.NoticeService;
import io.github.rose.notice.SendRequest;
import io.github.rose.notification.application.handler.NotificationSendConsumer;
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
public class RabbitNotificationSendConsumer implements NotificationSendConsumer {
    private final NoticeService noticeService;

    @RabbitListener(queues = RabbitNotificationSendProducer.QUEUE)
    public void consume(SendRequest sendRequest) {
        try {
            noticeService.send(sendRequest);
        } catch (Exception e) {
            log.error("[通知异步发送] 失败, requestId={}, traceId={}, error={}", sendRequest.getRequestId(), sendRequest.getTraceId(), e.getMessage(), e);
            // 可扩展：失败重试、死信队列等
        }
    }
}
