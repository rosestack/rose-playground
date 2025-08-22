package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillInvoiceService;
import io.github.rosestack.billing.domain.invoice.BillInvoice;
import io.github.rosestack.core.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 账单管理API控制器
 * <p>
 * 提供账单的生成、支付、查询、状态管理等API接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/invoices")
@RequiredArgsConstructor
@Slf4j
public class BillInvoiceController {

	private final BillInvoiceService invoiceService;

	/**
	 * 创建账单
	 */
	@PostMapping
	public ApiResponse<BillInvoice> createBill(@Valid @RequestBody BillInvoice bill) {
		log.info("Creating bill for subscription: {}", bill.getSubscriptionId());
		BillInvoice createdBill = invoiceService.createBill(bill);
		return ApiResponse.ok(createdBill);
	}

	/**
	 * 自动生成订阅账单
	 */
	@PostMapping("/generate/{subscriptionId}")
	public ApiResponse<BillInvoice> generateBillForSubscription(
		@PathVariable Long subscriptionId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

		log.info("Generating bill for subscription: {}, period: {} to {}",
			subscriptionId, periodStart, periodEnd);
		BillInvoice bill = invoiceService.generateBillForSubscription(subscriptionId, periodStart, periodEnd);
		return ApiResponse.ok(bill);
	}

	/**
	 * 批量生成账单
	 */
	@PostMapping("/generate-batch")
	public ApiResponse<Void> generateBillsForPeriod(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

		log.info("Generating bills for period: {} to {}", periodStart, periodEnd);
		invoiceService.generateBillsForPeriod(periodStart, periodEnd);
		return ApiResponse.ok();
	}

	/**
	 * 根据ID查询账单
	 */
	@GetMapping("/{id}")
	public ApiResponse<BillInvoice> getBill(@PathVariable Long id) {
		BillInvoice bill = invoiceService.findById(id);
		if (bill == null) {
			return ApiResponse.error("账单不存在");
		}
		return ApiResponse.ok(bill);
	}

	/**
	 * 根据账单编号查询账单
	 */
	@GetMapping("/bill-no/{billNo}")
	public ApiResponse<BillInvoice> getBillByBillNo(@PathVariable String billNo) {
		BillInvoice bill = invoiceService.findByBillNo(billNo);
		if (bill == null) {
			return ApiResponse.error("账单不存在");
		}
		return ApiResponse.ok(bill);
	}

	/**
	 * 根据订阅ID查询账单
	 */
	@GetMapping("/subscription/{subscriptionId}")
	public ApiResponse<List<BillInvoice>> getBillsBySubscription(@PathVariable Long subscriptionId) {
		List<BillInvoice> bills = invoiceService.findBySubscriptionId(subscriptionId);
		return ApiResponse.ok(bills);
	}

	/**
	 * 查询待支付账单
	 */
	@GetMapping("/pending-payment")
	public ApiResponse<List<BillInvoice>> getPendingPaymentBills() {
		List<BillInvoice> bills = invoiceService.findPendingPaymentBills();
		return ApiResponse.ok(bills);
	}

	/**
	 * 查询逾期账单
	 */
	@GetMapping("/overdue")
	public ApiResponse<List<BillInvoice>> getOverdueBills() {
		List<BillInvoice> bills = invoiceService.findOverdueBills();
		return ApiResponse.ok(bills);
	}

	/**
	 * 查询即将到期的账单
	 */
	@GetMapping("/due-soon")
	public ApiResponse<List<BillInvoice>> getBillsDueSoon(@RequestParam(defaultValue = "7") int days) {
		List<BillInvoice> bills = invoiceService.findBillsDueSoon(days);
		return ApiResponse.ok(bills);
	}

	/**
	 * 发布账单
	 */
	@PostMapping("/{id}/publish")
	public ApiResponse<BillInvoice> publishBill(@PathVariable Long id) {
		log.info("Publishing bill: {}", id);
		BillInvoice bill = invoiceService.publishBill(id);
		return ApiResponse.ok(bill);
	}

	/**
	 * 记录支付
	 */
	@PostMapping("/{id}/payment")
	public ApiResponse<BillInvoice> recordPayment(
		@PathVariable Long id,
		@RequestParam BigDecimal amount,
		@RequestParam(required = false) String paymentMethod) {

		log.info("Recording payment for bill: {}, amount: {}", id, amount);
		BillInvoice bill = invoiceService.recordPayment(id, amount, paymentMethod);
		return ApiResponse.ok(bill);
	}

	/**
	 * 作废账单
	 */
	@PostMapping("/{id}/void")
	public ApiResponse<Void> voidBill(
		@PathVariable Long id,
		@RequestParam(required = false) String reason) {

		log.info("Voiding bill: {}", id);
		invoiceService.voidBill(id, reason);
		return ApiResponse.ok();
	}

	/**
	 * 处理退款
	 */
	@PostMapping("/{id}/refund")
	public ApiResponse<BillInvoice> processRefund(
		@PathVariable Long id,
		@RequestParam BigDecimal refundAmount,
		@RequestParam(required = false) String reason) {

		log.info("Processing refund for bill: {}, amount: {}", id, refundAmount);
		BillInvoice bill = invoiceService.processRefund(id, refundAmount, reason);
		return ApiResponse.ok(bill);
	}

	/**
	 * 处理逾期账单（定时任务调用）
	 */
	@PostMapping("/process-overdue")
	public ApiResponse<Void> processOverdueBills() {
		log.info("Processing overdue bills");
		invoiceService.processOverdueBills();
		return ApiResponse.ok();
	}
}