package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notification.domain.entity.NotificationTemplate;
import io.github.rosestack.notification.domain.repository.NotificationTemplateRepository;
import io.github.rosestack.notification.infrastructure.mybatis.convert.NotificationTemplateConvert;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface NotificationTemplateMapper
	extends BaseMapper<NotificationTemplateEntity>, NotificationTemplateRepository {
	default Optional<NotificationTemplate> findById(String id) {
		NotificationTemplateEntity entity = selectById(id);
		return entity != null ? Optional.of(NotificationTemplateConvert.toDomain(entity)) : Optional.empty();
	}

	default Optional<NotificationTemplate> findByIdAndLang(String id, String lang) {
		LambdaQueryWrapper<NotificationTemplateEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(NotificationTemplateEntity::getId, id)
			.eq(NotificationTemplateEntity::getLang, lang)
			.last("LIMIT 1");
		NotificationTemplateEntity entity = selectOne(wrapper);
		return entity != null ? Optional.of(NotificationTemplateConvert.toDomain(entity)) : Optional.empty();
	}

	default void save(NotificationTemplate notificationTemplate) {
		insert(NotificationTemplateConvert.toEntity(notificationTemplate));
	}

	default void update(NotificationTemplate notificationTemplate) {
		updateById(NotificationTemplateConvert.toEntity(notificationTemplate));
	}

	default void delete(String id) {
		deleteById(id);
	}
}
