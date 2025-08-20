package io.github.rosestack.notice.infrastructure.mybatis.convert;

import io.github.rosestack.notice.domain.entity.NoticeChannel;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeChannelEntity;

public class NoticeChannelConvert {
    public static NoticeChannel toDomain(NoticeChannelEntity entity) {
        if (entity == null) return null;
        NoticeChannel domain = new NoticeChannel();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setChannelType(entity.getChannelType());
        domain.setConfig(entity.getConfig());
        domain.setEnabled(entity.isEnabled());
        return domain;
    }

    public static NoticeChannelEntity toEntity(NoticeChannel domain) {
        if (domain == null) return null;
        NoticeChannelEntity entity = new NoticeChannelEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setChannelType(domain.getChannelType());
        entity.setConfig(domain.getConfig());
        entity.setEnabled(domain.isEnabled());
        return entity;
    }
}
