package io.github.rose.notification.infra.mybatis.convert;

import io.github.rose.notification.domain.model.Notification;
import io.github.rose.notification.infra.mybatis.entity.NotificationEntity;

public class NotificationConvert {
    public static Notification toDomain(NotificationEntity entity) {
        if (entity == null) return null;
        Notification domain = new Notification();
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

    public static NotificationEntity toEntity(Notification domain) {
        if (domain == null) return null;
        NotificationEntity entity = new NotificationEntity();
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
