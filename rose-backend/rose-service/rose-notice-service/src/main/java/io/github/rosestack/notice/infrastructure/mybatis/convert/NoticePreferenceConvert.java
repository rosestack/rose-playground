package io.github.rosestack.notice.infrastructure.mybatis.convert;

import io.github.rosestack.notice.domain.entity.NoticePreference;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticePreferenceEntity;

public class NoticePreferenceConvert {
    public static NoticePreference toDomain(NoticePreferenceEntity entity) {
        if (entity == null) return null;
        NoticePreference domain = new NoticePreference();
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

    public static NoticePreferenceEntity toEntity(NoticePreference domain) {
        if (domain == null) return null;
        NoticePreferenceEntity entity = new NoticePreferenceEntity();
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
