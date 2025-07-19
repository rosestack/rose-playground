package io.github.rose.notification.infra.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.notification.domain.model.NotificationTemplate;
import io.github.rose.notification.domain.repository.NotificationTemplateRepository;
import io.github.rose.notification.infra.mybatis.convert.NotificationTemplateConvert;
import io.github.rose.notification.infra.mybatis.entity.NotificationTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplateEntity>, NotificationTemplateRepository {
    default NotificationTemplate findById(String id) {
        return NotificationTemplateConvert.toDomain(selectById(id));
    }

    @Select("SELECT * FROM notification_template WHERE id = #{id} AND lang = #{lang} LIMIT 1")
    NotificationTemplateEntity selectByIdAndLang(@Param("id") String id, @Param("lang") String lang);

    default NotificationTemplate findByIdAndLang(String id, String lang) {
        return NotificationTemplateConvert.toDomain(selectByIdAndLang(id, lang));
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
