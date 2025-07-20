package io.github.rose.notification.infra.mybatis.convert;

import io.github.rose.notification.domain.entity.NotificationPreference;
import io.github.rose.notification.infra.mybatis.entity.NotificationPreferenceEntity;

public class NotificationPreferenceConvert {
    public static NotificationPreference toDomain(NotificationPreferenceEntity entity) {
        if (entity == null) return null;
        NotificationPreference domain = new NotificationPreference();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setUserId(entity.getUserId());
        domain.setChannelType(entity.getChannelType() == null ? null : entity.getChannelType());
        domain.setEnabled(entity.isEnabled());
        domain.setQuietPeriod(entity.getQuietPeriod());
        domain.setChannelBlacklist(entity.getChannelBlacklist());
        domain.setChannelWhitelist(entity.getChannelWhitelist());
        domain.setFrequencyLimit(entity.getFrequencyLimit());
        return domain;
    }

    public static NotificationPreferenceEntity toEntity(NotificationPreference domain) {
        if (domain == null) return null;
        NotificationPreferenceEntity entity = new NotificationPreferenceEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setUserId(domain.getUserId());
        entity.setChannelType(domain.getChannelType() == null ? null : domain.getChannelType());
        entity.setEnabled(domain.isEnabled());
        entity.setQuietPeriod(domain.getQuietPeriod());
        entity.setChannelBlacklist(domain.getChannelBlacklist());
        entity.setChannelWhitelist(domain.getChannelWhitelist());
        entity.setFrequencyLimit(domain.getFrequencyLimit());
        return entity;
    }
}
