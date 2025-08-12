package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notification.domain.entity.NotificationTemplate;
import io.github.rosestack.notification.domain.repository.NotificationTemplateRepository;
import io.github.rosestack.notification.infrastructure.mybatis.convert.NotificationTemplateConvert;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationTemplateEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationTemplateMapper
        extends BaseMapper<NotificationTemplateEntity>, NotificationTemplateRepository {
    default Optional<NotificationTemplate> findById(String id) {
        NotificationTemplateEntity entity = selectById(id);
        return entity != null ? Optional.of(NotificationTemplateConvert.toDomain(entity)) : Optional.empty();
    }

    @Select("SELECT * FROM notification_template WHERE id = #{id} AND lang = #{lang} LIMIT 1")
    NotificationTemplateEntity selectByIdAndLang(@Param("id") String id, @Param("lang") String lang);

    default Optional<NotificationTemplate> findByIdAndLang(String id, String lang) {
        NotificationTemplateEntity entity = selectByIdAndLang(id, lang);
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
