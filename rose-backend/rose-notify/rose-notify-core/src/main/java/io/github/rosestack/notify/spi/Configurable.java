package io.github.rosestack.notify.spi;

import io.github.rosestack.notify.SenderConfiguration;

public interface Configurable {
    void configure(SenderConfiguration config);
}
