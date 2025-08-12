package io.github.rosestack.notice;

import io.github.rosestack.notice.sender.sms.SmsProvider;
import io.github.rosestack.notice.sender.sms.SmsProviderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmsProviderFactoryDestroyTest {
    @AfterEach
    void tearDown() {
        SmsProviderFactory.destroy();
        TestProvider.DESTROYED.set(false);
    }

    @Test
    void destroyShouldInvokeProviderDestroy() {
        SmsProviderFactory.register(new TestProvider());
        SenderConfiguration cfg = SenderConfiguration.builder()
                .channelType("sms")
                .config(Map.of("k", "v"))
                .build();
        SmsProvider p1 = SmsProviderFactory.getProvider("t", cfg);
        SmsProvider p2 = SmsProviderFactory.getProvider("t", cfg);
        Assertions.assertSame(p1, p2);
        SmsProviderFactory.destroy();
        Assertions.assertTrue(TestProvider.DESTROYED.get());
    }

    public static class TestProvider implements SmsProvider {
        static final AtomicBoolean DESTROYED = new AtomicBoolean(false);

        @Override
        public String getProviderType() {
            return "t";
        }

        @Override
        public String send(SendRequest request) {
            return request.getRequestId();
        }

        @Override
        public void destroy() {
            DESTROYED.set(true);
        }

        @Override
        public void configure(SenderConfiguration config) {
        }
    }
}
