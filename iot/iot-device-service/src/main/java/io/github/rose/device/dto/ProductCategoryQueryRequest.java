package io.github.rose.device.dto;

import lombok.Data;

/**
 * 产品分类查询请求
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
public class ProductCategoryQueryRequest {

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称（模糊查询）
     */
    private String name;

    /**
     * 分类标识符（模糊查询）
     */
    private String code;

    /**
     * 分类类型：STANDARD-标准行业分类，CUSTOM-自定义分类
     */
    private String type;

    /**
     * 分类状态：ACTIVE-激活，INACTIVE-未激活
     */
    private String status;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 10;
} 