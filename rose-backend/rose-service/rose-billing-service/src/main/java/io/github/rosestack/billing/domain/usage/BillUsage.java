package io.github.rosestack.billing.domain.usage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 使用量记录实体
 * 
 * 记录用户对各种功能的实际使用情况
 * 
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_usage")
public class BillUsage extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订阅ID
     */
    private Long subscriptionId;

    /**
     * 功能ID
     */
    private Long featureId;

    /**
     * 使用时间
     */
    private LocalDateTime usageTime;

    /**
     * 使用量
     * 如：API调用次数、存储空间GB数、处理时长等
     */
    private BigDecimal usageAmount;

    /**
     * 计量单位
     * 如：次、GB、小时、个等
     */
    private String unit;

    /**
     * 计费周期
     * 格式：YYYY-MM-DD，表示该使用量归属的计费月份
     * 用于按月汇总使用量和生成账单
     */
    private LocalDate billingPeriod;

    /**
     * 使用量元数据 - JSON格式存储详细信息
     * 
     * 示例：
     * API调用: {"endpoint": "/api/users", "method": "GET", "response_time": 120, "status_code": 200}
     * 存储使用: {"file_type": "image", "file_size": 1572864, "storage_class": "standard"}
     * 流量使用: {"region": "us-east-1", "cdn_hit": false, "bandwidth": 100.5}
     */
    private String metadata;

    /**
     * 检查使用量是否为正数
     */
    public boolean hasPositiveUsage() {
        return usageAmount != null && usageAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否在指定的计费周期内
     */
    public boolean isInBillingPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return billingPeriod != null &&
               !billingPeriod.isBefore(periodStart) &&
               !billingPeriod.isAfter(periodEnd);
    }

    /**
     * 检查是否为当月使用量
     */
    public boolean isCurrentMonth() {
        if (billingPeriod == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        return billingPeriod.getYear() == now.getYear() &&
               billingPeriod.getMonth() == now.getMonth();
    }

    /**
     * 获取使用量的可读描述
     */
    public String getUsageDescription() {
        if (usageAmount == null) {
            return "0";
        }

        String formattedAmount;
        if (usageAmount.scale() > 0) {
            formattedAmount = usageAmount.stripTrailingZeros().toPlainString();
        } else {
            formattedAmount = usageAmount.toPlainString();
        }

        return unit != null ? formattedAmount + " " + unit : formattedAmount;
    }

    /**
     * 设置当前月作为计费周期
     */
    public void setCurrentMonthAsBillingPeriod() {
        LocalDate now = LocalDate.now();
        this.billingPeriod = LocalDate.of(now.getYear(), now.getMonth(), 1);
    }

    /**
     * 设置指定日期所在月作为计费周期
     */
    public void setBillingPeriodFromDate(LocalDateTime dateTime) {
        if (dateTime != null) {
            LocalDate date = dateTime.toLocalDate();
            this.billingPeriod = LocalDate.of(date.getYear(), date.getMonth(), 1);
        }
    }

    /**
     * 增加使用量
     */
    public void addUsage(BigDecimal additionalAmount) {
        if (additionalAmount != null && additionalAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.usageAmount = this.usageAmount == null ? 
                additionalAmount : 
                this.usageAmount.add(additionalAmount);
        }
    }

    /**
     * 创建使用量记录的静态工厂方法
     */
    public static BillUsage createUsageRecord(String tenantId, Long subscriptionId, Long featureId, 
                                            BigDecimal amount, String unit) {
        BillUsage usage = new BillUsage();
        usage.setTenantId(tenantId);
        usage.setSubscriptionId(subscriptionId);
        usage.setFeatureId(featureId);
        usage.setUsageAmount(amount);
        usage.setUnit(unit);
        usage.setUsageTime(LocalDateTime.now());
        usage.setCurrentMonthAsBillingPeriod();
        return usage;
    }

    /**
     * 创建API调用使用量记录
     */
    public static BillUsage createApiUsage(String tenantId, Long subscriptionId, Long featureId, 
                                         String endpoint, String method, int statusCode) {
        BillUsage usage = createUsageRecord(tenantId, subscriptionId, featureId, BigDecimal.ONE, "次");
        String metadata = String.format(
            "{\"endpoint\":\"%s\",\"method\":\"%s\",\"status_code\":%d,\"timestamp\":\"%s\"}", 
            endpoint, method, statusCode, LocalDateTime.now()
        );
        usage.setMetadata(metadata);
        return usage;
    }

    /**
     * 创建存储使用量记录
     */
    public static BillUsage createStorageUsage(String tenantId, Long subscriptionId, Long featureId, 
                                             BigDecimal sizeInBytes, String fileType) {
        // 转换为GB
        BigDecimal sizeInGB = sizeInBytes.divide(BigDecimal.valueOf(1024 * 1024 * 1024), 6, BigDecimal.ROUND_HALF_UP);
        BillUsage usage = createUsageRecord(tenantId, subscriptionId, featureId, sizeInGB, "GB");
        String metadata = String.format(
            "{\"file_type\":\"%s\",\"file_size_bytes\":%s,\"timestamp\":\"%s\"}", 
            fileType, sizeInBytes.toPlainString(), LocalDateTime.now()
        );
        usage.setMetadata(metadata);
        return usage;
    }
}