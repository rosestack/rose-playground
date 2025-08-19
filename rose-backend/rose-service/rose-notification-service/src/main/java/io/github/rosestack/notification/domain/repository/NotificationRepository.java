package io.github.rosestack.notification.domain.repository;

import io.github.rosestack.notification.domain.entity.Notification;

import java.util.List;
import java.util.Optional;

/**
 * 通知仓储接口
 *
 * <p>定义通知实体的持久化操作。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public interface NotificationRepository {

	/**
	 * 根据ID查找通知
	 *
	 * @param id 通知ID
	 * @return 通知实体
	 */
	Optional<Notification> findById(String id);

	/**
	 * 保存通知
	 *
	 * @param notification 通知实体
	 */
	void save(Notification notification);

	/**
	 * 更新通知
	 *
	 * @param notification 通知实体
	 */
	void update(Notification notification);

	/**
	 * 删除通知
	 *
	 * @param id 通知ID
	 */
	void delete(String id);

	/**
	 * 根据请求ID查找通知
	 *
	 * @param requestId 请求ID
	 * @return 通知实体
	 */
	Optional<Notification> findByRequestId(String requestId);

	/**
	 * 根据租户ID查找通知列表
	 *
	 * @param tenantId 租户ID
	 * @return 通知列表
	 */
	List<Notification> findByTenantId(String tenantId);

	/**
	 * 根据目标查找通知列表
	 *
	 * @param target 通知目标
	 * @return 通知列表
	 */
	List<Notification> findByTarget(String target);

	/**
	 * 根据状态查找通知列表
	 *
	 * @param status 通知状态
	 * @return 通知列表
	 */
	List<Notification> findByStatus(String status);
}
