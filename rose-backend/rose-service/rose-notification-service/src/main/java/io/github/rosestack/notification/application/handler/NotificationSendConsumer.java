package io.github.rosestack.notification.application.handler;


import io.github.rosestack.notice.SendRequest;

public interface NotificationSendConsumer {
    void consume(SendRequest sendRequest);
}
