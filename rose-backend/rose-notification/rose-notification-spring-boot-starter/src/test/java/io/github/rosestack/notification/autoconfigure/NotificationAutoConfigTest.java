package io.github.rosestack.notification.autoconfigure;

import io.github.rosestack.notice.NoticeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NotificationAutoConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NotificationAutoConfig.class))
            .withPropertyValues(
                    "rose.notification.retryable=true",
                    "rose.notification.executor-core-size=2",
                    "rose.notification.sender-cache-max-size=500",
                    "rose.notification.sender-cache-expire-after-access-seconds=600",
                    "rose.notification.sms-provider-cache-max-size=500",
                    "rose.notification.sms-provider-cache-expire-after-access-seconds=600");

    @Test
    void autoConfigCreatesNoticeService() {
        contextRunner.run(ctx -> {
            NoticeService svc = ctx.getBean(NoticeService.class);
            Assertions.assertNotNull(svc);
        });
    }
}
