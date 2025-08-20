package io.github.rosestack.notice.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.rosestack.notice.domain.entity.NoticePreference;
import io.github.rosestack.notice.domain.repository.NoticePreferenceRepository;
import io.github.rosestack.notice.infrastructure.mybatis.convert.NoticePreferenceConvert;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticePreferenceEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticePreferenceMapper
        extends BaseMapper<NoticePreferenceEntity>, NoticePreferenceRepository {
    default Optional<NoticePreference> findById(String id) {
        NoticePreferenceEntity entity = selectById(id);
        return entity != null ? Optional.of(NoticePreferenceConvert.toDomain(entity)) : Optional.empty();
    }

    default void save(NoticePreference noticePreference) {
        insert(NoticePreferenceConvert.toEntity(noticePreference));
    }

    default void update(NoticePreference noticePreference) {
        updateById(NoticePreferenceConvert.toEntity(noticePreference));
    }

    default void delete(String id) {
        deleteById(id);
    }

    default NoticePreference findByUserId(String id) {
        NoticePreferenceEntity entity = selectOne(Wrappers.lambdaQuery(NoticePreferenceEntity.class)
                .eq(NoticePreferenceEntity::getUserId, id));
        return NoticePreferenceConvert.toDomain(entity);
    }
}
