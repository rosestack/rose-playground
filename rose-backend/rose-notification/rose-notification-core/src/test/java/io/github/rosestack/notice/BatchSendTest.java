package io.github.rosestack.notice;

import io.github.rosestack.notice.sender.SenderFactory;
import io.github.rosestack.notice.spi.Sender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class BatchSendTest {
    @AfterEach
    void tearDown() {
        SenderFactory.destroy();
    }

    @Test
    void batchShouldAggregateFailuresIntoResults() {
        SenderFactory.register("batch", new FailOnceSender());
        NoticeService svc = new NoticeService();

        SenderConfiguration cfg = SenderConfiguration.builder()
                .channelType("batch")
                .config(Map.of())
                .build();
        SendRequest r1 = SendRequest.builder()
                .requestId("r1")
                .target("t")
                .templateContent("c")
                .build();
        SendRequest r2 = SendRequest.builder()
                .requestId("r2")
                .target("t")
                .templateContent("c")
                .build();
        SendRequest r3 = SendRequest.builder()
                .requestId("r3")
                .target("t")
                .templateContent("c")
                .build();

        List<SendResult> res = svc.sendBatch(List.of(r1, r2, r3), cfg);
        Assertions.assertEquals(3, res.size());
        // FailOnceSender 的第二次调用抛异常，其余成功
        // 三个结果中恰有一个失败
        long success = res.stream().filter(SendResult::isSuccess).count();
        long failure = res.size() - success;
        Assertions.assertEquals(2, success);
        Assertions.assertEquals(1, failure);
    }

    static class FailOnceSender implements Sender {
        int called;

        @Override
        public String getChannelType() {
            return "batch";
        }

        @Override
        public String send(SendRequest sendRequest) {
            if (++called == 2) throw new RuntimeException("boom");
            return sendRequest.getRequestId();
        }

        @Override
        public void destroy() {
        }

        @Override
        public void configure(SenderConfiguration config) {
        }
    }
}
