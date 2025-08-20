package io.github.rosestack.notice.infrastructure.mybatis.convert;

import io.github.rosestack.notice.domain.entity.NoticeTemplateChannel;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeTemplateChannelEntity;

public class NoticeTemplateChannelConvert {
    public static NoticeTemplateChannel toDomain(NoticeTemplateChannelEntity entity) {
        if (entity == null) return null;
        NoticeTemplateChannel domain = new NoticeTemplateChannel();
        domain.setId(entity.getId());
        domain.setTemplateId(entity.getTemplateId());
        domain.setChannelId(entity.getChannelId());
        return domain;
    }

    public static NoticeTemplateChannelEntity toEntity(NoticeTemplateChannel domain) {
        if (domain == null) return null;
        NoticeTemplateChannelEntity entity = new NoticeTemplateChannelEntity();
        entity.setId(domain.getId());
        entity.setTemplateId(domain.getTemplateId());
        entity.setChannelId(domain.getChannelId());
        return entity;
    }
}
