package io.github.rose.notification.infra.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.rose.notification.infra.mybatis.convert.NotificationPreferenceConvert;
import io.github.rose.notification.infra.mybatis.entity.NotificationPreferenceEntity;
import io.github.rose.notification.domain.model.NotificationPreference;
import io.github.rose.notification.domain.repository.NotificationPreferenceRepository;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface NotificationPreferenceMapper extends BaseMapper<NotificationPreferenceEntity>, NotificationPreferenceRepository {
    default NotificationPreference findById(String id) {
        return NotificationPreferenceConvert.toDomain(selectById(id));
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
