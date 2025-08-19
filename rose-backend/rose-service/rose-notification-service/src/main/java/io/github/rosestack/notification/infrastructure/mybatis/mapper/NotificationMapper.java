package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notification.domain.entity.Notification;
import io.github.rosestack.notification.domain.repository.NotificationRepository;
import io.github.rosestack.notification.domain.value.NotificationStatus;
import io.github.rosestack.notification.infrastructure.mybatis.convert.NotificationConvert;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity>, NotificationRepository {
    default Optional<Notification> findById(String id) {
        NotificationEntity entity = selectById(id);
        return entity != null ? Optional.of(NotificationConvert.toDomain(entity)) : Optional.empty();
    }

    default void save(Notification notification) {
        insert(NotificationConvert.toEntity(notification));
    }

    default void update(Notification notification) {
        updateById(NotificationConvert.toEntity(notification));
    }

    default void delete(String id) {
        deleteById(id);
    }

    default Optional<Notification> findByRequestId(String requestId) {
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationEntity::getRequestId, requestId).last("LIMIT 1");
        NotificationEntity entity = selectOne(wrapper);
        return entity != null ? Optional.of(NotificationConvert.toDomain(entity)) : Optional.empty();
    }

    default List<Notification> findByTenantId(String tenantId) {
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationEntity::getTenantId, tenantId);
        return selectList(wrapper).stream().map(NotificationConvert::toDomain).toList();
    }

    default List<Notification> findByTarget(String target) {
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationEntity::getTarget, target);
        return selectList(wrapper).stream().map(NotificationConvert::toDomain).toList();
    }

    default List<Notification> findByStatus(NotificationStatus status) {
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationEntity::getStatus, status);
        return selectList(wrapper).stream().map(NotificationConvert::toDomain).toList();
    }
}
