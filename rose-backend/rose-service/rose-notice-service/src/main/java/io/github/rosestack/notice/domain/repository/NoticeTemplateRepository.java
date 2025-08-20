package io.github.rosestack.notice.domain.repository;

import io.github.rosestack.notice.domain.entity.NoticeTemplate;
import java.util.Optional;

/**
 * 通知模板仓储接口
 *
 * <p>定义通知模板实体的持久化操作。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public interface NoticeTemplateRepository {

    /**
     * 根据ID查找通知模板
     *
     * @param id 模板ID
     * @return 通知模板
     */
    Optional<NoticeTemplate> findById(String id);

    /**
     * 保存通知模板
     *
     * @param template 通知模板
     */
    void save(NoticeTemplate template);

    /**
     * 更新通知模板
     *
     * @param template 通知模板
     */
    void update(NoticeTemplate template);

    /**
     * 删除通知模板
     *
     * @param id 模板ID
     */
    void delete(String id);

    /**
     * 根据ID和语言查找通知模板
     *
     * @param id   模板ID
     * @param lang 语言
     * @return 通知模板
     */
    Optional<NoticeTemplate> findByIdAndLang(String id, String lang);
}
