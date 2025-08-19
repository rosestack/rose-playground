package io.github.rosestack.notice.spi;

import io.github.rosestack.notice.SenderConfiguration;

public interface Configurable {
	void configure(SenderConfiguration config);
}
