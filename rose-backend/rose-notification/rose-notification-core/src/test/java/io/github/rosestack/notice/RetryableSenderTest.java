package io.github.rosestack.notice;

import io.github.rosestack.notice.sender.RetryableSender;
import io.github.rosestack.notice.spi.Sender;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RetryableSenderTest {
    @Test
    void retryShouldSucceedAfterTemporaryFailures() {
        FlakySender flaky = new FlakySender();
        RetryableSender retry = new RetryableSender(flaky);
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("retry.maxAttempts", 5);
        cfg.put("retry.initialDelayMillis", 1);
        cfg.put("retry.jitterMillis", 1);
        retry.configure(
                SenderConfiguration.builder().channelType("test").config(cfg).build());

        SendRequest req = SendRequest.builder()
                .requestId("r1")
                .target("t")
                .templateContent("c")
                .build();
        String id = retry.send(req);
        Assertions.assertEquals("ok", id);
    }

    static class FlakySender implements Sender {
        int count;

        @Override
        public String getChannelType() {
            return "test";
        }

        @Override
        public String send(SendRequest sendRequest) {
            if (++count < 3) {
                throw new NoticeRetryableException("temporary");
            }
            return "ok";
        }

        @Override
        public void destroy() {}

        @Override
        public void configure(SenderConfiguration config) {}
    }
}
