package io.github.rosestack.billing.domain.outbox;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.domain.enums.OutboxEventStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 事件 Mapper 接口
 * <p>
 * 提供 Outbox 事件的数据访问方法
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface OutboxEventMapper extends BaseMapper<OutboxEvent> {

    /**
     * 查询待发布的事件
     * 
     * @param limit 限制数量
     * @return 待发布的事件列表
     */
    List<OutboxEvent> findPendingEvents(@Param("limit") int limit);

    /**
     * 查询可重试的失败事件
     * 
     * @param currentTime 当前时间
     * @param limit 限制数量
     * @return 可重试的失败事件列表
     */
    List<OutboxEvent> findRetryableFailedEvents(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("limit") int limit
    );

    /**
     * 更新事件状态
     * 
     * @param id 事件ID
     * @param status 新状态
     * @param errorMessage 错误信息
     * @param retryCount 重试次数
     * @param nextRetryTime 下次重试时间
     * @param publishedTime 发布时间
     * @return 更新的行数
     */
    int updateEventStatus(
            @Param("id") Long id,
            @Param("status") OutboxEventStatus status,
            @Param("errorMessage") String errorMessage,
            @Param("retryCount") Integer retryCount,
            @Param("nextRetryTime") LocalDateTime nextRetryTime,
            @Param("publishedTime") LocalDateTime publishedTime
    );

    /**
     * 删除已发布的旧事件
     * 
     * @param beforeTime 时间阈值
     * @return 删除的行数
     */
    int deletePublishedEventsBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计各状态的事件数量
     * 
     * @return 状态统计结果
     */
    List<OutboxEventStatusCount> countEventsByStatus();

    /**
     * 事件状态统计结果
     */
    class OutboxEventStatusCount {
        private OutboxEventStatus status;
        private Long count;

        public OutboxEventStatus getStatus() {
            return status;
        }

        public void setStatus(OutboxEventStatus status) {
            this.status = status;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}