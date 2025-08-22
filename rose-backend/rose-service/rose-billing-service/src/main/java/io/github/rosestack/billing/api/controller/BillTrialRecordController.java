package io.github.rosestack.billing.api.controller;

import io.github.rosestack.billing.application.service.BillTrialRecordService;
import io.github.rosestack.billing.domain.trial.BillTrialRecord;
import io.github.rosestack.core.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 试用记录管理API控制器
 *
 * 提供试用记录的创建、状态管理、统计分析等API接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing/trials")
@RequiredArgsConstructor
@Slf4j
public class BillTrialRecordController {

    private final BillTrialRecordService trialRecordService;

    /**
     * 开始试用
     */
    @PostMapping("/start")
    public ApiResponse<BillTrialRecord> startTrial(
            @RequestParam String tenantId,
            @RequestParam Long planId) {
        
        log.info("Starting trial for tenant: {}, plan: {}", tenantId, planId);
        BillTrialRecord trialRecord = trialRecordService.startTrial(tenantId, planId);
        return ApiResponse.ok(trialRecord);
    }

    /**
     * 检查试用资格
     */
    @GetMapping("/eligibility")
    public ApiResponse<Boolean> checkTrialEligibility(
            @RequestParam String tenantId,
            @RequestParam Long planId) {
        
        boolean eligible = trialRecordService.checkTrialEligibility(tenantId, planId);
        return ApiResponse.ok(eligible);
    }

    /**
     * 试用转为付费
     */
    @PostMapping("/convert")
    public ApiResponse<BillTrialRecord> convertTrialToPaid(
            @RequestParam String tenantId,
            @RequestParam Long planId) {
        
        log.info("Converting trial to paid for tenant: {}, plan: {}", tenantId, planId);
        BillTrialRecord trialRecord = trialRecordService.convertTrialToPaid(tenantId, planId);
        return ApiResponse.ok(trialRecord);
    }

    /**
     * 取消试用
     */
    @PostMapping("/cancel")
    public ApiResponse<Void> cancelTrial(
            @RequestParam String tenantId,
            @RequestParam Long planId,
            @RequestParam(required = false) String reason) {
        
        log.info("Cancelling trial for tenant: {}, plan: {}", tenantId, planId);
        trialRecordService.cancelTrial(tenantId, planId, reason);
        return ApiResponse.ok();
    }

    /**
     * 根据ID查询试用记录
     */
    @GetMapping("/{id}")
    public ApiResponse<BillTrialRecord> getTrialRecord(@PathVariable Long id) {
        BillTrialRecord trialRecord = trialRecordService.findById(id);
        if (trialRecord == null) {
            return ApiResponse.error("试用记录不存在");
        }
        return ApiResponse.ok(trialRecord);
    }

    /**
     * 根据租户和套餐查询试用记录
     */
    @GetMapping
    public ApiResponse<List<BillTrialRecord>> getTrialRecords(
            @RequestParam String tenantId,
            @RequestParam Long planId) {
        
        List<BillTrialRecord> records = trialRecordService.findByTenantAndPlan(tenantId, planId);
        return ApiResponse.ok(records);
    }

    /**
     * 获取最新试用记录
     */
    @GetMapping("/latest")
    public ApiResponse<BillTrialRecord> getLatestTrialRecord(
            @RequestParam String tenantId,
            @RequestParam Long planId) {
        
        BillTrialRecord record = trialRecordService.getLatestTrialRecord(tenantId, planId);
        if (record == null) {
            return ApiResponse.error("试用记录不存在");
        }
        return ApiResponse.ok(record);
    }

    /**
     * 获取即将过期的试用记录
     */
    @GetMapping("/expiring-soon")
    public ApiResponse<List<BillTrialRecord>> getTrialsExpiringSoon(
            @RequestParam(defaultValue = "3") int days) {
        
        List<BillTrialRecord> records = trialRecordService.getTrialsExpiringSoon(days);
        return ApiResponse.ok(records);
    }

    /**
     * 获取试用统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<BillTrialRecordService.TrialStatistics> getTrialStatistics() {
        BillTrialRecordService.TrialStatistics statistics = trialRecordService.getTrialStatistics();
        return ApiResponse.ok(statistics);
    }

    /**
     * 根据套餐获取试用统计
     */
    @GetMapping("/statistics/by-plan")
    public ApiResponse<Map<Long, Long>> getTrialCountsByPlan() {
        Map<Long, Long> statistics = trialRecordService.getTrialCountsByPlan();
        return ApiResponse.ok(statistics);
    }

    /**
     * 获取转换率分析
     */
    @GetMapping("/conversion-analysis")
    public ApiResponse<List<BillTrialRecordService.ConversionAnalysis>> getConversionAnalysis() {
        List<BillTrialRecordService.ConversionAnalysis> analysis = trialRecordService.getConversionAnalysis();
        return ApiResponse.ok(analysis);
    }

    /**
     * 处理过期试用（管理员接口）
     */
    @PostMapping("/process-expired")
    public ApiResponse<Void> processExpiredTrials() {
        log.info("Processing expired trials");
        trialRecordService.processExpiredTrials();
        return ApiResponse.ok();
    }
}