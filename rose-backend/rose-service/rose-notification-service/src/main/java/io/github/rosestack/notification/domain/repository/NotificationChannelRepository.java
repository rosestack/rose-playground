package io.github.rosestack.notification.domain.repository;

import io.github.rosestack.notification.domain.entity.NotificationChannel;

import java.util.List;
import java.util.Optional;

/**
 * 通知通道仓储接口
 *
 * <p>定义通知通道实体的持久化操作。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public interface NotificationChannelRepository {

    /**
     * 根据ID查找通知通道
     *
     * @param id 通道ID
     * @return 通知通道
     */
    Optional<NotificationChannel> findById(String id);

    /**
     * 保存通知通道
     *
     * @param channel 通知通道
     */
    void save(NotificationChannel channel);

    /**
     * 更新通知通道
     *
     * @param channel 通知通道
     */
    void update(NotificationChannel channel);

    /**
     * 删除通知通道
     *
     * @param id 通道ID
     */
    void delete(String id);

    /**
     * 根据类型和租户ID查找通知通道列表
     *
     * @param type     通道类型
     * @param tenantId 租户ID
     * @return 通知通道列表
     */
    List<NotificationChannel> findByTypeAndTenantId(String type, String tenantId);
}
