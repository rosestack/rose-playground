package io.github.rose.notification.infra.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue notificationSendQueue() {
        return new Queue(RabbitNotificationSendProducer.QUEUE, true);
    }
}
