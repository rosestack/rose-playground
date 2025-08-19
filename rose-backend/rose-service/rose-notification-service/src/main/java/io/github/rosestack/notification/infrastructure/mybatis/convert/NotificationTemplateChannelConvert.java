package io.github.rosestack.notification.infrastructure.mybatis.convert;

import io.github.rosestack.notification.domain.entity.NotificationTemplateChannel;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationTemplateChannelEntity;

public class NotificationTemplateChannelConvert {
	public static NotificationTemplateChannel toDomain(NotificationTemplateChannelEntity entity) {
		if (entity == null) return null;
		NotificationTemplateChannel domain = new NotificationTemplateChannel();
		domain.setId(entity.getId());
		domain.setTemplateId(entity.getTemplateId());
		domain.setChannelId(entity.getChannelId());
		return domain;
	}

	public static NotificationTemplateChannelEntity toEntity(NotificationTemplateChannel domain) {
		if (domain == null) return null;
		NotificationTemplateChannelEntity entity = new NotificationTemplateChannelEntity();
		entity.setId(domain.getId());
		entity.setTemplateId(domain.getTemplateId());
		entity.setChannelId(domain.getChannelId());
		return entity;
	}
}
