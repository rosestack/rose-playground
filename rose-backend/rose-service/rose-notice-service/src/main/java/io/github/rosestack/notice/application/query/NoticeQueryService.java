package io.github.rosestack.notice.application.query;

import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.domain.repository.NoticeRepository;
import io.github.rosestack.notice.shared.constant.NoticeConstants;
import io.github.rosestack.notice.shared.exception.NoticeException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 通知查询服务
 *
 * <p>处理通知相关的查询操作，遵循 CQRS 模式。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class NoticeQueryService {

    /**
     * 通知仓储
     */
    private final NoticeRepository noticeRepository;

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知信息
     * @throws NoticeException 当通知不存在时抛出异常
     */
    public Notice findById(String id) {
        return noticeRepository
                .findById(id)
                .orElseThrow(() -> new NoticeException(NoticeConstants.ErrorCode.NOTICE_NOT_FOUND));
    }

    /**
     * 根据租户ID查询通知列表
     *
     * @param tenantId 租户ID
     * @return 通知列表
     */
    public List<Notice> findByTenantId(String tenantId) {
        return noticeRepository.findByTenantId(tenantId);
    }

    /**
     * 根据目标查询通知列表
     *
     * @param target 通知目标
     * @return 通知列表
     */
    public List<Notice> findByTarget(String target) {
        return noticeRepository.findByTarget(target);
    }

    /**
     * 根据状态查询通知列表
     *
     * @param status 通知状态
     * @return 通知列表
     */
    public List<Notice> findByStatus(String status) {
        return noticeRepository.findByStatus(status);
    }

    /**
     * 分页查询通知
     *
     * @param tenantId 租户ID
     * @param page     页码（从0开始）
     * @param size     每页大小
     * @return 通知列表
     */
    public List<Notice> findByTenantIdWithPaging(String tenantId, int page, int size) {
        // TODO: 实现分页查询逻辑
        return noticeRepository.findByTenantId(tenantId);
    }
}
