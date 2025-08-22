package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillSubscriptionService;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.core.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅管理API控制器
 * <p>
 * 提供订阅的创建、状态管理、续费等API接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class BillSubscriptionController {

	private final BillSubscriptionService subscriptionService;

	/**
	 * 创建订阅
	 */
	@PostMapping
	public ApiResponse<BillSubscription> createSubscription(@Valid @RequestBody BillSubscription subscription) {
		log.info("Creating subscription for plan: {}", subscription.getPlanId());
		BillSubscription createdSubscription = subscriptionService.createSubscription(subscription);
		return ApiResponse.ok(createdSubscription);
	}

	/**
	 * 根据ID查询订阅
	 */
	@GetMapping("/{id}")
	public ApiResponse<BillSubscription> getSubscription(@PathVariable Long id) {
		BillSubscription subscription = subscriptionService.findById(id);
		if (subscription == null) {
			return ApiResponse.error("订阅不存在");
		}
		return ApiResponse.ok(subscription);
	}

	/**
	 * 根据订阅编号查询订阅
	 */
	@GetMapping("/sub-no/{subNo}")
	public ApiResponse<BillSubscription> getSubscriptionBySubNo(@PathVariable String subNo) {
		BillSubscription subscription = subscriptionService.findBySubNo(subNo);
		if (subscription == null) {
			return ApiResponse.error("订阅不存在");
		}
		return ApiResponse.ok(subscription);
	}

	/**
	 * 查询活跃的订阅
	 */
	@GetMapping("/active")
	public ApiResponse<List<BillSubscription>> getActiveSubscriptions() {
		List<BillSubscription> subscriptions = subscriptionService.findActiveSubscriptions();
		return ApiResponse.ok(subscriptions);
	}

	/**
	 * 查询需要续费的订阅
	 */
	@GetMapping("/need-renewal")
	public ApiResponse<List<BillSubscription>> getSubscriptionsNeedRenewal() {
		List<BillSubscription> subscriptions = subscriptionService.findSubscriptionsNeedRenewal();
		return ApiResponse.ok(subscriptions);
	}

	/**
	 * 查询即将过期的订阅
	 */
	@GetMapping("/expiring-soon")
	public ApiResponse<List<BillSubscription>> getExpiringSoonSubscriptions(
		@RequestParam(defaultValue = "7") int days) {
		List<BillSubscription> subscriptions = subscriptionService.findExpiringSoon(days);
		return ApiResponse.ok(subscriptions);
	}

	/**
	 * 查询超期未付费的订阅
	 */
	@GetMapping("/past-due")
	public ApiResponse<List<BillSubscription>> getPastDueSubscriptions() {
		List<BillSubscription> subscriptions = subscriptionService.findPastDueSubscriptions();
		return ApiResponse.ok(subscriptions);
	}

	/**
	 * 试用转正
	 */
	@PostMapping("/{id}/convert-trial")
	public ApiResponse<BillSubscription> convertTrialToActive(@PathVariable Long id) {
		log.info("Converting trial subscription to active: {}", id);
		BillSubscription subscription = subscriptionService.convertTrialToActive(id);
		return ApiResponse.ok(subscription);
	}

	/**
	 * 续费订阅
	 */
	@PostMapping("/{id}/renew")
	public ApiResponse<BillSubscription> renewSubscription(@PathVariable Long id) {
		log.info("Renewing subscription: {}", id);
		BillSubscription subscription = subscriptionService.renewSubscription(id);
		return ApiResponse.ok(subscription);
	}

	/**
	 * 暂停订阅
	 */
	@PostMapping("/{id}/suspend")
	public ApiResponse<Void> suspendSubscription(
		@PathVariable Long id,
		@RequestParam(required = false) String reason) {
		log.info("Suspending subscription: {}", id);
		subscriptionService.suspendSubscription(id, reason);
		return ApiResponse.ok();
	}

	/**
	 * 取消订阅（优雅取消）
	 */
	@PostMapping("/{id}/cancel")
	public ApiResponse<Void> cancelSubscription(@PathVariable Long id) {
		log.info("Cancelling subscription: {}", id);
		subscriptionService.cancelSubscription(id);
		return ApiResponse.ok();
	}

	/**
	 * 立即取消订阅
	 */
	@PostMapping("/{id}/cancel-immediately")
	public ApiResponse<Void> cancelSubscriptionImmediately(@PathVariable Long id) {
		log.info("Cancelling subscription immediately: {}", id);
		subscriptionService.cancelSubscriptionImmediately(id);
		return ApiResponse.ok();
	}

	/**
	 * 重新激活订阅
	 */
	@PostMapping("/{id}/reactivate")
	public ApiResponse<BillSubscription> reactivateSubscription(@PathVariable Long id) {
		log.info("Reactivating subscription: {}", id);
		BillSubscription subscription = subscriptionService.reactivateSubscription(id);
		return ApiResponse.ok(subscription);
	}

	/**
	 * 处理过期订阅（定时任务调用）
	 */
	@PostMapping("/process-expired")
	public ApiResponse<Void> processExpiredSubscriptions() {
		log.info("Processing expired subscriptions");
		subscriptionService.processExpiredSubscriptions();
		return ApiResponse.ok();
	}
}