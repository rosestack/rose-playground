package io.github.rose.notification.config;

import io.github.rose.notice.NoticeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Configuration
public class NoticeConfig {
    @Bean
    public NoticeService noticeService() {
        return new NoticeService();
    }
}
