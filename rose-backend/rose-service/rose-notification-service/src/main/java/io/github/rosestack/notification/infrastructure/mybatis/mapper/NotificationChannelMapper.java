package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notification.domain.entity.NotificationChannel;
import io.github.rosestack.notification.domain.repository.NotificationChannelRepository;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.infrastructure.mybatis.convert.NotificationChannelConvert;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationChannelEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationChannelMapper
        extends BaseMapper<NotificationChannelEntity>, NotificationChannelRepository {
    default Optional<NotificationChannel> findById(String id) {
        NotificationChannelEntity entity = selectById(id);
        return entity != null ? Optional.of(NotificationChannelConvert.toDomain(entity)) : Optional.empty();
    }

    default List<NotificationChannel> findByTypeAndTenantId(NotificationChannelType channelType, String tenantId) {
        LambdaQueryWrapper<NotificationChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannelEntity::getChannelType, channelType)
               .eq(NotificationChannelEntity::getTenantId, tenantId);
        return selectList(wrapper).stream()
                .map(NotificationChannelConvert::toDomain)
                .toList();
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
