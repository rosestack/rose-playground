package io.github.rose.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.rose.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品分类实体
 * <p>
 * 用于管理物联网产品的分类信息，支持树形结构分类管理。
 * 支持标准行业分类和自定义分类两种类型。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_product_category")
public class ProductCategory extends BaseEntity {

    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类标识符
     */
    @TableField("code")
    private String code;

    /**
     * 父分类ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类层级
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 分类图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 分类类型：标准行业分类/自定义分类
     */
    @TableField("type")
    private CategoryType type;

    /**
     * 关联的物模型模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 分类状态
     */
    @TableField("status")
    private CategoryStatus status;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 分类类型枚举
     */
    public enum CategoryType {
        /** 标准行业分类 */
        STANDARD,
        /** 自定义分类 */
        CUSTOM
    }

    /**
     * 分类状态枚举
     */
    public enum CategoryStatus {
        /** 激活 */
        ACTIVE,
        /** 未激活 */
        INACTIVE
    }
} 