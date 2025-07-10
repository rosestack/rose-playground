package io.github.rose.notification.infra.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.notification.domain.model.Notification;
import io.github.rose.notification.domain.repository.NotificationRepository;
import io.github.rose.notification.infra.mybatis.convert.NotificationConvert;
import io.github.rose.notification.infra.mybatis.entity.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity>, NotificationRepository {
    default Notification findById(String id) {
        return NotificationConvert.toDomain(selectById(id));
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

    default Notification findByRequestId(String requestId) {
        return NotificationConvert.toDomain(selectByRequestId(requestId));
    }
}
