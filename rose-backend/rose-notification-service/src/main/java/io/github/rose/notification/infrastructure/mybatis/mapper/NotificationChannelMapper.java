package io.github.rose.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.notification.domain.entity.NotificationChannel;
import io.github.rose.notification.domain.repository.NotificationChannelRepository;
import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.infrastructure.mybatis.convert.NotificationChannelConvert;
import io.github.rose.notification.infrastructure.mybatis.entity.NotificationChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;


@Mapper
public interface NotificationChannelMapper extends BaseMapper<NotificationChannelEntity>, NotificationChannelRepository {
    default Optional<NotificationChannel> findById(String id) {
        NotificationChannelEntity entity = selectById(id);
        return entity != null ? Optional.of(NotificationChannelConvert.toDomain(entity)) : Optional.empty();
    }

    @Select("SELECT * FROM notification_channel WHERE channel_type = #{channelType} AND tenant_id = #{tenantId}")
    List<NotificationChannelEntity> selectByTypeAndTenantId(@Param("channelType") String channelType, @Param("tenantId") String tenantId);

    default List<NotificationChannel> findByTypeAndTenantId(NotificationChannelType channelType, String tenantId) {
        return selectByTypeAndTenantId(channelType.name(), tenantId).stream()
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
