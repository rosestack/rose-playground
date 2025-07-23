package io.github.rose.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品分类实体类
 * <p>
 * 用于管理物联网产品的分类信息，支持多级分类结构和物模型模板关联。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("iot_product_category")
public class ProductCategory implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 分类类型：STANDARD-标准行业分类，CUSTOM-自定义分类
     */
    @TableField("type")
    private CategoryType type;

    /**
     * 关联的物模型模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 分类状态：ACTIVE-激活，INACTIVE-未激活
     */
    @TableField("status")
    private CategoryStatus status;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 分类类型枚举
     */
    public enum CategoryType {
        /**
         * 标准行业分类
         */
        STANDARD,
        /**
         * 自定义分类
         */
        CUSTOM
    }

    /**
     * 分类状态枚举
     */
    public enum CategoryStatus {
        /**
         * 激活
         */
        ACTIVE,
        /**
         * 未激活
         */
        INACTIVE
    }
} 