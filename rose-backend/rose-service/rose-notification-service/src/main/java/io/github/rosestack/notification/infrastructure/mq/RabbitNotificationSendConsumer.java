package io.github.rosestack.notification.infrastructure.mq;

import io.github.rosestack.notice.NoticeService;
import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notification.application.handler.NotificationSendConsumer;
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
			noticeService.send(sendRequest, null);
		} catch (Exception e) {
			log.error("[通知异步发送] 失败, requestId={},error={}", sendRequest.getRequestId(), e.getMessage(), e);
			// 可扩展：失败重试、死信队列等
		}
	}
}
