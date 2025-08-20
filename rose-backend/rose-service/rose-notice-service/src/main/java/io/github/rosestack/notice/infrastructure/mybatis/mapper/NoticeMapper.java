package io.github.rosestack.notice.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.domain.repository.NoticeRepository;
import io.github.rosestack.notice.domain.value.NoticeStatus;
import io.github.rosestack.notice.infrastructure.mybatis.convert.NoticeConvert;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<NoticeEntity>, NoticeRepository {
    default Optional<Notice> findById(String id) {
        NoticeEntity entity = selectById(id);
        return entity != null ? Optional.of(NoticeConvert.toDomain(entity)) : Optional.empty();
    }

    default void save(Notice notice) {
        insert(NoticeConvert.toEntity(notice));
    }

    default void update(Notice notice) {
        updateById(NoticeConvert.toEntity(notice));
    }

    default void delete(String id) {
        deleteById(id);
    }

    default Optional<Notice> findByRequestId(String requestId) {
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeEntity::getRequestId, requestId).last("LIMIT 1");
        NoticeEntity entity = selectOne(wrapper);
        return entity != null ? Optional.of(NoticeConvert.toDomain(entity)) : Optional.empty();
    }

    default List<Notice> findByTenantId(String tenantId) {
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeEntity::getTenantId, tenantId);
        return selectList(wrapper).stream().map(NoticeConvert::toDomain).toList();
    }

    default List<Notice> findByTarget(String target) {
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeEntity::getTarget, target);
        return selectList(wrapper).stream().map(NoticeConvert::toDomain).toList();
    }

    default List<Notice> findByStatus(NoticeStatus status) {
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeEntity::getStatus, status);
        return selectList(wrapper).stream().map(NoticeConvert::toDomain).toList();
    }
}
