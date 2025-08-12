package io.github.rosestack.notification.domain.repository;

import io.github.rosestack.notification.domain.entity.NotificationTemplateChannel;
import io.github.rosestack.notification.domain.value.NotificationChannelType;

import java.util.List;

public interface NotificationTemplateChannelRepository {
    List<NotificationTemplateChannel> findByTemplateId(String templateId);

    List<NotificationTemplateChannel> findByChannelType(NotificationChannelType channelType);

    void save(NotificationTemplateChannel notificationTemplateChannel);

    void update(NotificationTemplateChannel notificationTemplateChannel);

    void delete(String templateId, NotificationChannelType channelType);
}
