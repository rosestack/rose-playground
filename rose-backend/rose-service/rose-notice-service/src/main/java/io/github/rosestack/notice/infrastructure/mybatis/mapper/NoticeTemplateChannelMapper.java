package io.github.rosestack.notice.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notice.domain.entity.NoticeTemplateChannel;
import io.github.rosestack.notice.domain.repository.NoticeTemplateChannelRepository;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeTemplateChannelEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeTemplateChannelMapper
        extends BaseMapper<NoticeTemplateChannelEntity>, NoticeTemplateChannelRepository {
    List<NoticeTemplateChannel> findByTemplateId(String templateId);

    List<NoticeTemplateChannel> findByChannelType(NoticeChannelType channelType);

    void save(NoticeTemplateChannel noticeTemplateChannel);

    void update(NoticeTemplateChannel noticeTemplateChannel);

    void delete(String templateId, NoticeChannelType channelType);
}
