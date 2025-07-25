package io.github.rosestack.notice.support;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.spi.RateLimiter;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class NoopRateLimiter implements RateLimiter {
    @Override
    public boolean allow(SendRequest request) {
        return true;
    }
}
