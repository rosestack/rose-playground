package io.github.rose.device.dto;

import io.github.rose.device.entity.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品分类查询请求DTO
 *
 * @author rose
 * @since 2024-01-01
 */
@Data
@Schema(description = "产品分类查询请求")
public class ProductCategoryQueryRequest {

    @Schema(description = "分类名称", example = "智能家居")
    private String name;

    @Schema(description = "分类标识符", example = "smart_home")
    private String code;

    @Schema(description = "父分类ID", example = "1")
    private Long parentId;

    @Schema(description = "分类类型", example = "CUSTOM")
    private ProductCategory.CategoryType type;

    @Schema(description = "分类状态", example = "ACTIVE")
    private ProductCategory.CategoryStatus status;
}