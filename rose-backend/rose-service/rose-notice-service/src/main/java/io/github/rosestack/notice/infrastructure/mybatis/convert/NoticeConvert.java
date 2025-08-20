package io.github.rosestack.notice.infrastructure.mybatis.convert;

import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeEntity;

public class NoticeConvert {
    public static Notice toDomain(NoticeEntity entity) {
        if (entity == null) return null;
        Notice domain = new Notice();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setChannelId(entity.getChannelId());
        domain.setTarget(entity.getTarget());
        domain.setContent(entity.getContent());
        domain.setTemplateId(entity.getTemplateId());
        domain.setStatus(entity.getStatus() == null ? null : entity.getStatus());
        domain.setFailReason(entity.getFailReason());
        domain.setSendTime(entity.getSendTime());
        domain.setReadTime(entity.getReadTime());
        domain.setTraceId(entity.getTraceId());
        domain.setRequestId(entity.getRequestId());
        domain.setChannelType(entity.getChannelType());
        return domain;
    }

    public static NoticeEntity toEntity(Notice domain) {
        if (domain == null) return null;
        NoticeEntity entity = new NoticeEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setChannelId(domain.getChannelId());
        entity.setTarget(domain.getTarget());
        entity.setContent(domain.getContent());
        entity.setTemplateId(domain.getTemplateId());
        entity.setStatus(domain.getStatus() == null ? null : domain.getStatus());
        entity.setFailReason(domain.getFailReason());
        entity.setSendTime(domain.getSendTime());
        entity.setReadTime(domain.getReadTime());
        entity.setTraceId(domain.getTraceId());
        entity.setRequestId(domain.getRequestId());
        entity.setChannelType(domain.getChannelType());
        return entity;
    }
}
