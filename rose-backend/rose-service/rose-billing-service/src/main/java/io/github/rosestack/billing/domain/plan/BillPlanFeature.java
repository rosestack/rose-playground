package io.github.rosestack.billing.domain.plan;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.core.model.HasStatus;
import io.github.rosestack.mybatis.audit.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 套餐功能关联表实体类
 *
 * 定义套餐与功能的关联关系，包括功能值配置
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_plan_feature")
public class BillPlanFeature extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐ID
     */
    private Long planId;

    /**
     * 功能ID
     */
    private Long featureId;

    /**
     * 功能值配置
     *
     * 功能值配置说明：
     *
     * 1. QUOTA类型功能
     *    - 配置方式：具体数值，如 "100" 表示100GB存储
     *    - 示例：存储空间="100", 用户数量="50", 项目数量="10"
     *    - 含义：该套餐提供的配额限制
     *
     * 2. USAGE类型功能
     *    - 配置方式：免费额度，如 "1000" 表示1000次免费调用
     *    - 示例：API调用="1000", 短信发送="500", 邮件发送="2000"
     *    - 含义：超出此额度后开始按量计费
     *
     * 3. SWITCH类型功能
     *    - 配置方式：开关状态，"enabled" 或 "disabled"
     *    - 示例：高级分析="enabled", 白标定制="disabled"
     *    - 含义：功能是否对该套餐开放
     */
    private String featureValue;

    /**
     * 状态
     * ACTIVE: 激活
     * INACTIVE: 禁用
     */
    private String status;

    /**
     * 检查关联是否激活
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 激活关联
     */
    public void activate() {
        this.status = "ACTIVE";
    }

    /**
     * 禁用关联
     */
    public void deactivate() {
        this.status = "INACTIVE";
    }

    /**
     * 获取功能值作为数值
     * 用于QUOTA和USAGE类型功能
     */
    public Long getFeatureValueAsNumber() {
        if (featureValue == null || featureValue.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(featureValue.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 获取功能值作为小数
     * 用于支持小数的功能值
     */
    public Double getFeatureValueAsDecimal() {
        if (featureValue == null || featureValue.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(featureValue.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 检查是否为开关功能的启用状态
     * 用于SWITCH类型功能
     */
    public boolean isFeatureEnabled() {
        return "enabled".equalsIgnoreCase(featureValue);
    }

    /**
     * 检查是否为开关功能的禁用状态
     * 用于SWITCH类型功能
     */
    public boolean isFeatureDisabled() {
        return "disabled".equalsIgnoreCase(featureValue);
    }

    /**
     * 设置功能值为数值
     */
    public void setFeatureValueAsNumber(Long value) {
        this.featureValue = value != null ? value.toString() : "0";
    }

    /**
     * 设置功能值为小数
     */
    public void setFeatureValueAsDecimal(Double value) {
        this.featureValue = value != null ? value.toString() : "0.0";
    }

    /**
     * 启用开关功能
     */
    public void enableFeature() {
        this.featureValue = "enabled";
    }

    /**
     * 禁用开关功能
     */
    public void disableFeature() {
        this.featureValue = "disabled";
    }

    /**
     * 验证功能值配置是否有效
     */
    public boolean isValidFeatureValue() {
        if (featureValue == null || featureValue.trim().isEmpty()) {
            return false;
        }

        String trimmedValue = featureValue.trim();

        // 检查是否为开关值
        if ("enabled".equalsIgnoreCase(trimmedValue) || "disabled".equalsIgnoreCase(trimmedValue)) {
            return true;
        }

        // 检查是否为有效数值
        try {
            Double.parseDouble(trimmedValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取功能配置的完整标识
     * 格式：planId:featureId:value
     */
    public String getConfigKey() {
        return String.format("%d:%d:%s", planId, featureId, featureValue);
    }
}
