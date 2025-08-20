package io.github.rosestack.notice.domain.repository;

import io.github.rosestack.notice.domain.entity.NoticeTemplateChannel;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import java.util.List;

public interface NoticeTemplateChannelRepository {
    List<NoticeTemplateChannel> findByTemplateId(String templateId);

    List<NoticeTemplateChannel> findByChannelType(NoticeChannelType channelType);

    void save(NoticeTemplateChannel noticeTemplateChannel);

    void update(NoticeTemplateChannel noticeTemplateChannel);

    void delete(String templateId, NoticeChannelType channelType);
}
