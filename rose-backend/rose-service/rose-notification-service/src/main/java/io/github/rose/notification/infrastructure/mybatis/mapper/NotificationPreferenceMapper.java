package io.github.rose.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.rose.notification.domain.entity.NotificationPreference;
import io.github.rose.notification.domain.repository.NotificationPreferenceRepository;
import io.github.rose.notification.infrastructure.mybatis.convert.NotificationPreferenceConvert;
import io.github.rose.notification.infrastructure.mybatis.entity.NotificationPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;


@Mapper
public interface NotificationPreferenceMapper extends BaseMapper<NotificationPreferenceEntity>, NotificationPreferenceRepository {
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
