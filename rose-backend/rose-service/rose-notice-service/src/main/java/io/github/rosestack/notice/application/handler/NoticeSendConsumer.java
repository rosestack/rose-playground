package io.github.rosestack.notice.application.handler;

import io.github.rosestack.notify.SendRequest;

public interface NoticeSendConsumer {
    void consume(SendRequest sendRequest);
}
