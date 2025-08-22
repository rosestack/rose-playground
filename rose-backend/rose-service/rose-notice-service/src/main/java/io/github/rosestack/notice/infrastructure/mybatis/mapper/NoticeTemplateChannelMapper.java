package io.github.rosestack.notice.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notice.domain.entity.NoticeTemplateChannel;
import io.github.rosestack.notice.domain.repository.NoticeTemplateChannelRepository;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.infrastructure.mybatis.convert.NoticeTemplateChannelConvert;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeTemplateChannelEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeTemplateChannelMapper
        extends BaseMapper<NoticeTemplateChannelEntity>, NoticeTemplateChannelRepository {
    
    default List<NoticeTemplateChannel> findByTemplateId(String templateId) {
        LambdaQueryWrapper<NoticeTemplateChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeTemplateChannelEntity::getTemplateId, templateId);
        return selectList(wrapper).stream()
                .map(NoticeTemplateChannelConvert::toDomain)
                .toList();
    }

    default List<NoticeTemplateChannel> findByChannelType(NoticeChannelType channelType) {
        // 注意：由于Entity中没有channelType字段，这里需要根据实际业务逻辑进行修改
        // 可能需要通过channelId关联查询，或者修改Entity结构
        LambdaQueryWrapper<NoticeTemplateChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeTemplateChannelEntity::getChannelId, channelType.name()); // 假设channelId存储的是channelType的名称
        return selectList(wrapper).stream()
                .map(NoticeTemplateChannelConvert::toDomain)
                .toList();
    }

    default void save(NoticeTemplateChannel noticeTemplateChannel) {
        insert(NoticeTemplateChannelConvert.toEntity(noticeTemplateChannel));
    }

    default void update(NoticeTemplateChannel noticeTemplateChannel) {
        updateById(NoticeTemplateChannelConvert.toEntity(noticeTemplateChannel));
    }

    default void delete(String templateId, NoticeChannelType channelType) {
        LambdaQueryWrapper<NoticeTemplateChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeTemplateChannelEntity::getTemplateId, templateId)
                .eq(NoticeTemplateChannelEntity::getChannelId, channelType.name()); // 假设channelId存储的是channelType的名称
        delete(wrapper);
    }
}
