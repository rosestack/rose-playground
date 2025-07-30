package io.github.rosestack.notice.sender;

import io.github.rosestack.notice.NoticeRetryableException;
import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.SenderConfiguration;
import io.github.rosestack.notice.spi.AbstractConfigure;
import io.github.rosestack.notice.spi.Sender;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Retryable;

/**
 * Sender 委托类，集成 Spring Retry。
 */
@RequiredArgsConstructor
public class RetryableSender extends AbstractConfigure implements Sender {
    private final Sender delegate;

    @Override
    public String getChannelType() {
        return delegate.getChannelType();
    }

    @Override
    @Retryable(value = NoticeRetryableException.class)
    public String send(SendRequest request) {
        return delegate.send(request);
    }

    @Override
    public void destroy() {}

    @Override
    public void doConfigure(SenderConfiguration config) throws Exception {}
}
