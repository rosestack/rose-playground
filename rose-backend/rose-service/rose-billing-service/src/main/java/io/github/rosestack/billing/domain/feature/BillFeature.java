package io.github.rosestack.billing.domain.feature;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.domain.enums.FeatureStatus;
import io.github.rosestack.billing.domain.enums.FeatureType;
import io.github.rosestack.billing.domain.enums.ResetPeriod;
import io.github.rosestack.billing.domain.enums.ValueScope;
import io.github.rosestack.mybatis.audit.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 功能表实体类
 *
 * 定义系统中的计费功能，包括功能类型、计量单位、重置周期等配置
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_feature")
public class BillFeature extends BaseTenantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 功能代码 - 唯一标识
     * 示例：api_calls、storage、users、advanced_analytics
     */
    private String code;

    /**
     * 功能名称
     * 示例：API调用、存储空间、用户数量、高级分析
     */
    private String name;

    /**
     * 功能描述
     */
    private String description;

    /**
     * 功能类型
     * QUOTA: 配额限制型 - 有固定的使用上限，不按使用量计费
     * USAGE: 使用量计费型 - 按实际使用量计费，可能有免费额度
     * SWITCH: 开关功能型 - 功能的开启/关闭，通常不涉及使用量
     */
    private FeatureType type;

    /**
     * 计量单位
     * 示例：次、GB、个、小时等
     */
    private String unit;

    /**
     * 重置周期
     * DAY: 日重置
     * MONTH: 月重置
     * YEAR: 年重置
     * NEVER: 不重置
     */
    private ResetPeriod resetPeriod;

    /**
     * 功能范围
     * PER_SUBSCRIPTION: 按订阅 - 功能配额在整个订阅范围内共享
     * PER_SEAT: 按席位 - 功能配额按每个用户席位独立计算
     */
    private ValueScope valueScope;

    /**
     * 功能状态
     * ACTIVE: 激活
     * INACTIVE: 禁用
     */
    private FeatureStatus status;

    /**
     * 检查功能是否激活
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /**
     * 检查是否为配额限制型功能
     */
    public boolean isQuotaFeature() {
        return FeatureType.QUOTA == type;
    }

    /**
     * 检查是否为使用量计费型功能
     */
    public boolean isUsageFeature() {
        return FeatureType.USAGE == type;
    }

    /**
     * 检查是否为开关功能型
     */
    public boolean isSwitchFeature() {
        return FeatureType.SWITCH == type;
    }

    /**
     * 检查是否按订阅范围计算
     */
    public boolean isPerSubscription() {
        return ValueScope.PER_SUBSCRIPTION == valueScope;
    }

    /**
     * 检查是否按席位计算
     */
    public boolean isPerSeat() {
        return ValueScope.PER_SEAT == valueScope;
    }

    /**
     * 检查是否需要重置
     */
    public boolean needsReset() {
        return resetPeriod != null && resetPeriod != ResetPeriod.NEVER;
    }

    /**
     * 检查是否为系统级功能（租户ID为"0"）
     */
    public boolean isSystemFeature() {
        return getTenantId() != null && getTenantId().equals("0");
    }

    /**
     * 检查是否为租户专属功能
     */
    public boolean isTenantSpecific() {
        return getTenantId() != null && !getTenantId().equals("0");
    }

    /**
     * 激活功能
     */
    public void activate() {
        this.status = FeatureStatus.ACTIVE;
    }

    /**
     * 禁用功能
     */
    public void deactivate() {
        this.status = FeatureStatus.INACTIVE;
    }

    /**
     * 获取功能的完整标识
     * 格式：tenant_id:code
     */
    public String getFullCode() {
        return String.format("%s:%s", getTenantId(), code);
    }

    /**
     * 验证功能配置是否有效
     */
    public boolean isValidConfiguration() {
        // 基本字段验证
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (type == null) {
            return false;
        }
        if (resetPeriod == null) {
            return false;
        }
        if (valueScope == null) {
            return false;
        }

        // 业务逻辑验证
        // 开关功能型不应该有计量单位
        if (type == FeatureType.SWITCH && unit != null && !unit.trim().isEmpty()) {
            return false;
        }

        // 配额限制型和使用量计费型应该有计量单位
        if ((type == FeatureType.QUOTA || type == FeatureType.USAGE)
            && (unit == null || unit.trim().isEmpty())) {
            return false;
        }

        return true;
    }

    /**
     * 检查功能是否可以被删除
     * 仅非系统级功能可以被删除
     */
    public boolean canBeDeleted() {
        return !isSystemFeature();
    }

    /**
     * 检查功能是否有效
     * 等同于isValidConfiguration方法
     */
    public boolean isValidFeature() {
        return isValidConfiguration();
    }
}
