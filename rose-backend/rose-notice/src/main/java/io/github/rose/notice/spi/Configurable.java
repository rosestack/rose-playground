package io.github.rose.notice.spi;

import io.github.rose.notice.SenderConfiguration;

public interface Configurable {
    void configure(SenderConfiguration config);
}
