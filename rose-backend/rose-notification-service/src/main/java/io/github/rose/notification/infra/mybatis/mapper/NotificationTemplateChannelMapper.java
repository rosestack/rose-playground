package io.github.rose.notification.infra.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.notification.domain.entity.NotificationTemplateChannel;
import io.github.rose.notification.domain.repository.NotificationTemplateChannelRepository;
import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.infra.mybatis.entity.NotificationTemplateChannelEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationTemplateChannelMapper extends BaseMapper<NotificationTemplateChannelEntity>, NotificationTemplateChannelRepository {
    List<NotificationTemplateChannel> findByTemplateId(String templateId);

    List<NotificationTemplateChannel> findByChannelType(NotificationChannelType channelType);

    void save(NotificationTemplateChannel notificationTemplateChannel);

    void update(NotificationTemplateChannel notificationTemplateChannel);

    void delete(String templateId, NotificationChannelType channelType);
}
