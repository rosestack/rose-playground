package io.github.rosestack.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 功能类型枚举
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum FeatureType {

    /**
     * 配额限制型 - 有固定的使用上限，不按使用量计费
     * 特点：有固定上限，包含在套餐基础费用中
     * 示例：存储空间(100GB)、API调用次数(10000次/月)、用户数量(50个)
     */
    QUOTA("配额限制型"),

    /**
     * 使用量计费型 - 按实际使用量计费，可能有免费额度
     * 特点：按实际使用量计费，超出免费额度后按使用量收费
     * 示例：CDN流量、短信发送、计算资源使用时长
     */
    USAGE("使用量计费型"),

    /**
     * 开关功能型 - 功能的开启/关闭，通常不涉及使用量
     * 特点：功能开关，开启后按周期收费
     * 示例：高级分析、白标定制、优先技术支持
     */
    SWITCH("开关功能型");

    private final String description;
}