package io.github.rosestack.notice.infrastructure.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue noticeSendQueue() {
        return new Queue(RabbitNoticeSendProducer.QUEUE, true);
    }
}
