package io.github.rose.notice.support;

import io.github.rose.notice.SendRequest;
import io.github.rose.notice.spi.RateLimiter;

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
