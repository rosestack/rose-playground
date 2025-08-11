package io.github.rosestack.billing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 事件发布器（最小实现：仅日志）
 * 后续可替换为 MQ/Webhook 发送
 */
@Slf4j
@Component
public class OutboxPublisher {
    public void publish(String eventType, String payload) {
        log.info("Outbox publish: type={}, payload={}", eventType, payload);
    }
}

