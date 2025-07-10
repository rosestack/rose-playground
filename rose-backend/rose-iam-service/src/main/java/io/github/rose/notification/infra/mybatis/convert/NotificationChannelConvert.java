package io.github.rose.notification.infra.mybatis.convert;

import io.github.rose.notification.domain.model.NotificationChannel;
import io.github.rose.notification.infra.mybatis.entity.NotificationChannelEntity;

public class NotificationChannelConvert {
    public static NotificationChannel toDomain(NotificationChannelEntity entity) {
        if (entity == null) return null;
        NotificationChannel domain = new NotificationChannel();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setChannelType(entity.getChannelType());
        domain.setConfig(entity.getConfig());
        domain.setEnabled(entity.isEnabled());
        return domain;
    }

    public static NotificationChannelEntity toEntity(NotificationChannel domain) {
        if (domain == null) return null;
        NotificationChannelEntity entity = new NotificationChannelEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setChannelType(domain.getChannelType());
        entity.setConfig(domain.getConfig());
        entity.setEnabled(domain.isEnabled());
        return entity;
    }
}
