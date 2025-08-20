package io.github.rosestack.notice.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notice.domain.entity.NoticeChannel;
import io.github.rosestack.notice.domain.repository.NoticeChannelRepository;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.infrastructure.mybatis.convert.NoticeChannelConvert;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeChannelEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeChannelMapper
        extends BaseMapper<NoticeChannelEntity>, NoticeChannelRepository {
    default Optional<NoticeChannel> findById(String id) {
        NoticeChannelEntity entity = selectById(id);
        return entity != null ? Optional.of(NoticeChannelConvert.toDomain(entity)) : Optional.empty();
    }

    default List<NoticeChannel> findByTypeAndTenantId(NoticeChannelType channelType, String tenantId) {
        LambdaQueryWrapper<NoticeChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeChannelEntity::getChannelType, channelType)
                .eq(NoticeChannelEntity::getTenantId, tenantId);
        return selectList(wrapper).stream()
                .map(NoticeChannelConvert::toDomain)
                .toList();
    }

    default void save(NoticeChannel channel) {
        insert(NoticeChannelConvert.toEntity(channel));
    }

    default void update(NoticeChannel channel) {
        updateById(NoticeChannelConvert.toEntity(channel));
    }

    default void delete(String id) {
        deleteById(id);
    }
}
