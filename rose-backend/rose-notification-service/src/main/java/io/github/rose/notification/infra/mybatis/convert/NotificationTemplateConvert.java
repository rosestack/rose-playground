package io.github.rose.notification.infra.mybatis.convert;

import io.github.rose.notification.domain.entity.NotificationTemplate;
import io.github.rose.notification.infra.mybatis.entity.NotificationTemplateEntity;

public class NotificationTemplateConvert {
    public static NotificationTemplate toDomain(NotificationTemplateEntity entity) {
        if (entity == null) return null;
        NotificationTemplate domain = new NotificationTemplate();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setName(entity.getName());
        domain.setType(entity.getType());
        domain.setContent(entity.getContent());
        domain.setEnabled(entity.isEnabled());
        domain.setVersion(entity.getVersion());
        domain.setLang(entity.getLang());
        domain.setDescription(entity.getDescription());
        return domain;
    }

    public static NotificationTemplateEntity toEntity(NotificationTemplate domain) {
        if (domain == null) return null;
        NotificationTemplateEntity entity = new NotificationTemplateEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setContent(domain.getContent());
        entity.setEnabled(domain.isEnabled());
        entity.setVersion(domain.getVersion());
        entity.setLang(domain.getLang());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}
