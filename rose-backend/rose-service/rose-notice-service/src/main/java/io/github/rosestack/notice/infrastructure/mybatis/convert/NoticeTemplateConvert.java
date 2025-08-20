package io.github.rosestack.notice.infrastructure.mybatis.convert;

import io.github.rosestack.notice.domain.entity.NoticeTemplate;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeTemplateEntity;

public class NoticeTemplateConvert {
    public static NoticeTemplate toDomain(NoticeTemplateEntity entity) {
        if (entity == null) return null;
        NoticeTemplate domain = new NoticeTemplate();
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

    public static NoticeTemplateEntity toEntity(NoticeTemplate domain) {
        if (domain == null) return null;
        NoticeTemplateEntity entity = new NoticeTemplateEntity();
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
