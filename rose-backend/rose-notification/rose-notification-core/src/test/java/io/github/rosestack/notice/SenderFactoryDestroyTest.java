package io.github.rosestack.notice;

import io.github.rosestack.notice.sender.SenderFactory;
import io.github.rosestack.notice.spi.Sender;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SenderFactoryDestroyTest {
    public static class TestSender implements Sender {
        static final AtomicBoolean DESTROYED = new AtomicBoolean(false);

        @Override
        public String getChannelType() {
            return "test";
        }

        @Override
        public String send(SendRequest sendRequest) {
            return sendRequest.getRequestId();
        }

        @Override
        public void destroy() {
            DESTROYED.set(true);
        }

        @Override
        public void configure(SenderConfiguration config) {}
    }

    @AfterEach
    void tearDown() {
        SenderFactory.destroy();
        TestSender.DESTROYED.set(false);
    }

    @Test
    void destroyShouldInvokeSenderDestroy() {
        SenderFactory.register("test", new TestSender());
        SenderConfiguration cfg = SenderConfiguration.builder()
                .channelType("test")
                .config(Map.of("k", "v"))
                .build();
        Sender s1 = SenderFactory.getSender("test", cfg);
        Sender s2 = SenderFactory.getSender("test", cfg);
        Assertions.assertSame(s1, s2);
        SenderFactory.destroy();
        Assertions.assertTrue(TestSender.DESTROYED.get());
    }
}
