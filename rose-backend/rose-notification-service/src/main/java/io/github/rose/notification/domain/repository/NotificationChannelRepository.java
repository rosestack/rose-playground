package io.github.rose.notification.domain.repository;

import io.github.rose.notification.domain.model.NotificationChannel;

public interface NotificationChannelRepository {
    NotificationChannel findById(String id);

    void save(NotificationChannel channel);

    void update(NotificationChannel channel);

    void delete(String id);
}
