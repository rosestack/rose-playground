package io.github.rosestack.spring.boot.notify.config;

import io.github.rosestack.notify.NotifyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NotifyAutoConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NotifyAutoConfig.class))
            .withPropertyValues(
                    "rose.notify.retryable=true",
                    "rose.notify.executor-core-size=2",
                    "rose.notify.sender-cache-max-size=500",
                    "rose.notify.sender-cache-expire-after-access-seconds=600",
                    "rose.notify.sms-provider-cache-max-size=500",
                    "rose.notify.sms-provider-cache-expire-after-access-seconds=600");

    @Test
    void autoConfigCreatesNoticeService() {
        contextRunner.run(ctx -> {
            NotifyService svc = ctx.getBean(NotifyService.class);
            Assertions.assertNotNull(svc);
        });
    }
}
