package io.github.rose.device.vo;

import io.github.rose.device.entity.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品分类响应VO
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@Schema(description = "产品分类响应")
public class ProductCategoryVO {

    @Schema(description = "分类ID", example = "1")
    private Long id;

    @Schema(description = "分类名称", example = "智能家居")
    private String name;

    @Schema(description = "分类标识符", example = "smart_home")
    private String code;

    @Schema(description = "父分类ID", example = "1")
    private Long parentId;

    @Schema(description = "分类层级", example = "1")
    private Integer level;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "分类图标", example = "icon-home")
    private String icon;

    @Schema(description = "分类描述", example = "智能家居设备分类")
    private String description;

    @Schema(description = "分类类型", example = "CUSTOM")
    private ProductCategory.CategoryType type;

    @Schema(description = "关联的物模型模板ID", example = "1")
    private Long templateId;

    @Schema(description = "分类状态", example = "ACTIVE")
    private ProductCategory.CategoryStatus status;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "创建人", example = "admin")
    private String createdBy;

    @Schema(description = "更新人", example = "admin")
    private String updatedBy;

    @Schema(description = "版本号", example = "1")
    private Integer version;

    @Schema(description = "子分类列表")
    private List<ProductCategoryVO> children;
} 