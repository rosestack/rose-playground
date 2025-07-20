package io.github.rose.notification.domain.repository;

import io.github.rose.notification.domain.entity.NotificationPreference;

import java.util.Optional;

public interface NotificationPreferenceRepository {
    Optional<NotificationPreference> findById(String id);
    void save(NotificationPreference preference);
    void update(NotificationPreference preference);
    void delete(String id);

}
