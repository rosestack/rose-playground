package io.github.rose.device.vo;

import io.github.rose.device.entity.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品分类视图对象
 * <p>
 * 用于返回产品分类信息，包含分类的基本信息和子分类列表。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@Schema(description = "产品分类视图对象")
public class ProductCategoryVO {

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "智能家居")
    private String name;

    /**
     * 分类标识符
     */
    @Schema(description = "分类标识符", example = "smart_home")
    private String code;

    /**
     * 父分类ID
     */
    @Schema(description = "父分类ID", example = "1")
    private Long parentId;

    /**
     * 分类层级
     */
    @Schema(description = "分类层级", example = "1")
    private Integer level;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "0")
    private Integer sortOrder;

    /**
     * 分类图标
     */
    @Schema(description = "分类图标", example = "icon-home")
    private String icon;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", example = "智能家居设备分类")
    private String description;

    /**
     * 分类类型
     */
    @Schema(description = "分类类型", example = "CUSTOM")
    private ProductCategory.CategoryType type;

    /**
     * 关联的物模型模板ID
     */
    @Schema(description = "关联的物模型模板ID", example = "1")
    private Long templateId;

    /**
     * 分类状态
     */
    @Schema(description = "分类状态", example = "ACTIVE")
    private ProductCategory.CategoryStatus status;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "admin")
    private String createdBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人", example = "admin")
    private String updatedBy;

    /**
     * 子分类列表
     */
    @Schema(description = "子分类列表")
    private List<ProductCategoryVO> children;

    /**
     * 是否有子分类
     */
    @Schema(description = "是否有子分类", example = "true")
    private Boolean hasChildren;

    /**
     * 产品数量
     */
    @Schema(description = "产品数量", example = "10")
    private Integer productCount;
} 