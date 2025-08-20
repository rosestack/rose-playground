package io.github.rosestack.notice.infrastructure.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.notice.domain.entity.NoticeTemplate;
import io.github.rosestack.notice.domain.repository.NoticeTemplateRepository;
import io.github.rosestack.notice.infrastructure.mybatis.convert.NoticeTemplateConvert;
import io.github.rosestack.notice.infrastructure.mybatis.entity.NoticeTemplateEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeTemplateMapper
        extends BaseMapper<NoticeTemplateEntity>, NoticeTemplateRepository {
    default Optional<NoticeTemplate> findById(String id) {
        NoticeTemplateEntity entity = selectById(id);
        return entity != null ? Optional.of(NoticeTemplateConvert.toDomain(entity)) : Optional.empty();
    }

    default Optional<NoticeTemplate> findByIdAndLang(String id, String lang) {
        LambdaQueryWrapper<NoticeTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeTemplateEntity::getId, id)
                .eq(NoticeTemplateEntity::getLang, lang)
                .last("LIMIT 1");
        NoticeTemplateEntity entity = selectOne(wrapper);
        return entity != null ? Optional.of(NoticeTemplateConvert.toDomain(entity)) : Optional.empty();
    }

    default void save(NoticeTemplate noticeTemplate) {
        insert(NoticeTemplateConvert.toEntity(noticeTemplate));
    }

    default void update(NoticeTemplate noticeTemplate) {
        updateById(NoticeTemplateConvert.toEntity(noticeTemplate));
    }

    default void delete(String id) {
        deleteById(id);
    }
}
