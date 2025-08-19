package io.github.rosestack.notification.application.query;

import io.github.rosestack.notification.domain.entity.Notification;
import io.github.rosestack.notification.domain.repository.NotificationRepository;
import io.github.rosestack.notification.shared.constant.NotificationConstants;
import io.github.rosestack.notification.shared.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class NotificationQueryService {

	/**
	 * 通知仓储
	 */
	private final NotificationRepository notificationRepository;

	/**
	 * 根据ID查询通知
	 *
	 * @param id 通知ID
	 * @return 通知信息
	 * @throws NotificationException 当通知不存在时抛出异常
	 */
	public Notification findById(String id) {
		return notificationRepository
			.findById(id)
			.orElseThrow(() -> new NotificationException(NotificationConstants.ErrorCode.NOTIFICATION_NOT_FOUND));
	}

	/**
	 * 根据租户ID查询通知列表
	 *
	 * @param tenantId 租户ID
	 * @return 通知列表
	 */
	public List<Notification> findByTenantId(String tenantId) {
		return notificationRepository.findByTenantId(tenantId);
	}

	/**
	 * 根据目标查询通知列表
	 *
	 * @param target 通知目标
	 * @return 通知列表
	 */
	public List<Notification> findByTarget(String target) {
		return notificationRepository.findByTarget(target);
	}

	/**
	 * 根据状态查询通知列表
	 *
	 * @param status 通知状态
	 * @return 通知列表
	 */
	public List<Notification> findByStatus(String status) {
		return notificationRepository.findByStatus(status);
	}

	/**
	 * 分页查询通知
	 *
	 * @param tenantId 租户ID
	 * @param page     页码（从0开始）
	 * @param size     每页大小
	 * @return 通知列表
	 */
	public List<Notification> findByTenantIdWithPaging(String tenantId, int page, int size) {
		// TODO: 实现分页查询逻辑
		return notificationRepository.findByTenantId(tenantId);
	}
}
