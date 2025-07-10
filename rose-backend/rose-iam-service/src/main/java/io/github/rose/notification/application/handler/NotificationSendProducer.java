package io.github.rose.notification.application.handler;


import io.github.rose.notice.SendRequest;

public interface NotificationSendProducer {
    void send(SendRequest sendRequest);
}
