package io.github.rose.notification.domain.repository;

import io.github.rose.notification.domain.model.NotificationPreference;

public interface NotificationPreferenceRepository {
    NotificationPreference findById(String id);
    void save(NotificationPreference preference);
    void update(NotificationPreference preference);
    void delete(String id);

}
