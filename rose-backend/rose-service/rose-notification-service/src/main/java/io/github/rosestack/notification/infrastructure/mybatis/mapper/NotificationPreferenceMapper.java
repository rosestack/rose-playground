package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.rosestack.notification.domain.entity.NotificationPreference;
import io.github.rosestack.notification.domain.repository.NotificationPreferenceRepository;
import io.github.rosestack.notification.infrastructure.mybatis.convert.NotificationPreferenceConvert;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface NotificationPreferenceMapper
	extends BaseMapper<NotificationPreferenceEntity>, NotificationPreferenceRepository {
	default Optional<NotificationPreference> findById(String id) {
		NotificationPreferenceEntity entity = selectById(id);
		return entity != null ? Optional.of(NotificationPreferenceConvert.toDomain(entity)) : Optional.empty();
	}

	default void save(NotificationPreference notificationPreference) {
		insert(NotificationPreferenceConvert.toEntity(notificationPreference));
	}

	default void update(NotificationPreference notificationPreference) {
		updateById(NotificationPreferenceConvert.toEntity(notificationPreference));
	}

	default void delete(String id) {
		deleteById(id);
	}

	default NotificationPreference findByUserId(String id) {
		NotificationPreferenceEntity entity = selectOne(Wrappers.lambdaQuery(NotificationPreferenceEntity.class)
			.eq(NotificationPreferenceEntity::getUserId, id));
		return NotificationPreferenceConvert.toDomain(entity);
	}
}
