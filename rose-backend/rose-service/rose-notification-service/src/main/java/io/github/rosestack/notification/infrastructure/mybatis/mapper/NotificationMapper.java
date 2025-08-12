package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notification.domain.entity.Notification;
import io.github.rosestack.notification.domain.repository.NotificationRepository;
import io.github.rosestack.notification.domain.value.NotificationStatus;
import io.github.rosestack.notification.infrastructure.mybatis.convert.NotificationConvert;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    @Select("SELECT * FROM notification WHERE request_id = #{requestId} LIMIT 1")
    NotificationEntity selectByRequestId(@Param("requestId") String requestId);

    default Optional<Notification> findByRequestId(String requestId) {
        NotificationEntity entity = selectByRequestId(requestId);
        return entity != null ? Optional.of(NotificationConvert.toDomain(entity)) : Optional.empty();
    }

    @Select("SELECT * FROM notification WHERE tenant_id = #{tenantId}")
    List<NotificationEntity> selectByTenantId(@Param("tenantId") String tenantId);

    default List<Notification> findByTenantId(String tenantId) {
        return selectByTenantId(tenantId).stream()
                .map(NotificationConvert::toDomain)
                .toList();
    }

    @Select("SELECT * FROM notification WHERE target = #{target}")
    List<NotificationEntity> selectByTarget(@Param("target") String target);

    default List<Notification> findByTarget(String target) {
        return selectByTarget(target).stream()
                .map(NotificationConvert::toDomain)
                .toList();
    }

    @Select("SELECT * FROM notification WHERE status = #{status}")
    List<NotificationEntity> selectByStatus(@Param("status") String status);

    default List<Notification> findByStatus(NotificationStatus status) {
        return selectByStatus(status.name()).stream()
                .map(NotificationConvert::toDomain)
                .toList();
    }
}
