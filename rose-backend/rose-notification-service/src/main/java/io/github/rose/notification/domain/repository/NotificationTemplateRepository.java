package io.github.rose.notification.domain.repository;

import io.github.rose.notification.domain.model.NotificationTemplate;

public interface NotificationTemplateRepository {
    NotificationTemplate findById(String id);

    void save(NotificationTemplate template);

    void update(NotificationTemplate template);

    void delete(String id);

    NotificationTemplate findByIdAndLang(String id, String lang);
}
