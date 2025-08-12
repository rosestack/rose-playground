package io.github.rosestack.notification.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notification.domain.entity.NotificationTemplateChannel;
import io.github.rosestack.notification.domain.repository.NotificationTemplateChannelRepository;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.infrastructure.mybatis.entity.NotificationTemplateChannelEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationTemplateChannelMapper
        extends BaseMapper<NotificationTemplateChannelEntity>, NotificationTemplateChannelRepository {
    List<NotificationTemplateChannel> findByTemplateId(String templateId);

    List<NotificationTemplateChannel> findByChannelType(NotificationChannelType channelType);

    void save(NotificationTemplateChannel notificationTemplateChannel);

    void update(NotificationTemplateChannel notificationTemplateChannel);

    void delete(String templateId, NotificationChannelType channelType);
}
