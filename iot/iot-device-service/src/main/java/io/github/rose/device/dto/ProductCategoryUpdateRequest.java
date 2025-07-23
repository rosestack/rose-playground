package io.github.rose.device.dto;

import io.github.rose.device.entity.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 产品分类更新请求DTO
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@Schema(description = "产品分类更新请求")
public class ProductCategoryUpdateRequest {

    @Schema(description = "分类名称", example = "智能家居")
    @Size(max = 128, message = "分类名称长度不能超过128个字符")
    private String name;

    @Schema(description = "分类标识符", example = "smart_home")
    @NotBlank(message = "分类标识符不能为空")
    @Size(max = 64, message = "分类标识符长度不能超过64个字符")
    private String code;

    @Schema(description = "父分类ID", example = "1")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "分类图标", example = "icon-home")
    @Size(max = 255, message = "分类图标长度不能超过255个字符")
    private String icon;

    @Schema(description = "分类描述", example = "智能家居设备分类")
    private String description;

    @Schema(description = "分类类型", example = "CUSTOM")
    private ProductCategory.CategoryType type;

    @Schema(description = "关联的物模型模板ID", example = "1")
    private Long templateId;

    @Schema(description = "分类状态", example = "ACTIVE")
    private ProductCategory.CategoryStatus status;

    @Schema(description = "版本号", example = "1")
    @NotNull(message = "版本号不能为空")
    private Integer version;
} 