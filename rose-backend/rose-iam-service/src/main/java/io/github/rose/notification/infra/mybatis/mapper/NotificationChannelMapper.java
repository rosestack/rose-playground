package io.github.rose.notification.infra.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.notification.domain.model.NotificationChannel;
import io.github.rose.notification.domain.repository.NotificationChannelRepository;
import io.github.rose.notification.infra.mybatis.convert.NotificationChannelConvert;
import io.github.rose.notification.infra.mybatis.entity.NotificationChannelEntity;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface NotificationChannelMapper extends BaseMapper<NotificationChannelEntity>, NotificationChannelRepository {
    default NotificationChannel findById(String id) {
        return NotificationChannelConvert.toDomain(selectById(id));
    }

    default void save(NotificationChannel channel) {
        insert(NotificationChannelConvert.toEntity(channel));
    }

    default void update(NotificationChannel channel) {
        updateById(NotificationChannelConvert.toEntity(channel));
    }

    default void delete(String id) {
        deleteById(id);
    }
}
