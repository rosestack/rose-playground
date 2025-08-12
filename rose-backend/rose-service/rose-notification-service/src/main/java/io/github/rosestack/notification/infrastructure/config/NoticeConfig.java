package io.github.rosestack.notification.infrastructure.config;

import io.github.rosestack.notice.NoticeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知服务配置类
 *
 * <p>配置通知发送相关的 Bean 组件。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Configuration
public class NoticeConfig {
    @Bean
    public NoticeService noticeService() {
        return new NoticeService();
    }
}
