package io.github.rose.device.dto;

import io.github.rose.device.entity.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 产品分类创建请求DTO
 * <p>
 * 用于创建新的产品分类，包含分类的基本信息和验证规则。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@Schema(description = "产品分类创建请求")
public class ProductCategoryCreateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 128, message = "分类名称长度必须在1-128个字符之间")
    @Schema(description = "分类名称", example = "智能家居", required = true)
    private String name;

    /**
     * 分类标识符
     */
    @NotBlank(message = "分类标识符不能为空")
    @Size(min = 1, max = 64, message = "分类标识符长度必须在1-64个字符之间")
    @Schema(description = "分类标识符", example = "smart_home", required = true)
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
    @Size(max = 255, message = "分类图标长度不能超过255个字符")
    @Schema(description = "分类图标", example = "icon-home")
    private String icon;

    /**
     * 分类描述
     */
    @Size(max = 1000, message = "分类描述长度不能超过1000个字符")
    @Schema(description = "分类描述", example = "智能家居设备分类")
    private String description;

    /**
     * 分类类型
     */
    @NotNull(message = "分类类型不能为空")
    @Schema(description = "分类类型", example = "CUSTOM", required = true)
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
} 