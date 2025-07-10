package io.github.rose.notification.domain.repository;

import io.github.rose.notification.domain.model.Notification;

public interface NotificationRepository {
    Notification findById(String id);

    void save(Notification notification);

    void update(Notification notification);

    void delete(String id);

    Notification findByRequestId(String requestId);
}
