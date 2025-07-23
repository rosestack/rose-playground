package io.github.rose.device.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.rose.common.model.ApiResponse;
import io.github.rose.device.dto.ProductCategoryCreateRequest;
import io.github.rose.device.dto.ProductCategoryQueryRequest;
import io.github.rose.device.dto.ProductCategoryUpdateRequest;
import io.github.rose.device.service.ProductCategoryService;
import io.github.rose.device.vo.ProductCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品分类管理控制器
 * <p>
 * 提供产品分类的增删改查接口，支持树形结构管理和分页查询。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/product/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "产品分类管理", description = "产品分类的增删改查接口")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    /**
     * 创建产品分类
     *
     * @param request 创建请求
     * @return 创建的分类信息
     */
    @PostMapping
    @Operation(summary = "创建产品分类", description = "创建新的产品分类")
    public ApiResponse<ProductCategoryVO> createCategory(@Valid @RequestBody ProductCategoryCreateRequest request) {
        log.info("开始创建产品分类，请求参数：{}", request);
        
        try {
            ProductCategoryVO category = productCategoryService.createCategory(request);
            log.info("产品分类创建成功，分类ID：{}", category.getId());
            return ApiResponse.success(category);
        } catch (Exception e) {
            log.error("创建产品分类失败，请求参数：{}", request, e);
            return ApiResponse.error(500, "创建产品分类失败：" + e.getMessage());
        }
    }

    /**
     * 更新产品分类
     *
     * @param categoryId 分类ID
     * @param request 更新请求
     * @return 更新后的分类信息
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "更新产品分类", description = "根据分类ID更新产品分类信息")
    public ApiResponse<ProductCategoryVO> updateCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long categoryId,
            @Valid @RequestBody ProductCategoryUpdateRequest request) {
        log.info("开始更新产品分类，分类ID：{}，请求参数：{}", categoryId, request);
        
        try {
            request.setId(categoryId);
            ProductCategoryVO category = productCategoryService.updateCategory(request);
            log.info("产品分类更新成功，分类ID：{}", categoryId);
            return ApiResponse.success(category);
        } catch (Exception e) {
            log.error("更新产品分类失败，分类ID：{}，请求参数：{}", categoryId, request, e);
            return ApiResponse.error(500, "更新产品分类失败：" + e.getMessage());
        }
    }

    /**
     * 删除产品分类
     *
     * @param categoryId 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "删除产品分类", description = "根据分类ID删除产品分类")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long categoryId) {
        log.info("开始删除产品分类，分类ID：{}", categoryId);
        
        try {
            boolean success = productCategoryService.deleteCategory(categoryId);
            if (success) {
                log.info("产品分类删除成功，分类ID：{}", categoryId);
                return ApiResponse.success();
            } else {
                log.warn("产品分类删除失败，分类ID：{}", categoryId);
                return ApiResponse.error(500, "删除产品分类失败");
            }
        } catch (Exception e) {
            log.error("删除产品分类失败，分类ID：{}", categoryId, e);
            return ApiResponse.error(500, "删除产品分类失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取产品分类
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "获取产品分类详情", description = "根据分类ID获取产品分类详细信息")
    public ApiResponse<ProductCategoryVO> getCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long categoryId) {
        log.info("开始获取产品分类详情，分类ID：{}", categoryId);
        
        try {
            ProductCategoryVO category = productCategoryService.getCategoryById(categoryId);
            if (category != null) {
                log.info("获取产品分类详情成功，分类ID：{}", categoryId);
                return ApiResponse.success(category);
            } else {
                log.warn("产品分类不存在，分类ID：{}", categoryId);
                return ApiResponse.error(404, "产品分类不存在");
            }
        } catch (Exception e) {
            log.error("获取产品分类详情失败，分类ID：{}", categoryId, e);
            return ApiResponse.error(500, "获取产品分类详情失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询产品分类
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询产品分类", description = "根据条件分页查询产品分类列表")
    public ApiResponse<IPage<ProductCategoryVO>> pageCategories(ProductCategoryQueryRequest request) {
        log.info("开始分页查询产品分类，请求参数：{}", request);
        
        try {
            IPage<ProductCategoryVO> page = productCategoryService.pageCategories(request);
            log.info("分页查询产品分类成功，总记录数：{}", page.getTotal());
            return ApiResponse.success(page);
        } catch (Exception e) {
            log.error("分页查询产品分类失败，请求参数：{}", request, e);
            return ApiResponse.error(500, "分页查询产品分类失败：" + e.getMessage());
        }
    }

    /**
     * 查询分类树
     *
     * @param tenantId 租户ID
     * @return 分类树列表
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分类树", description = "查询产品分类的树形结构")
    public ApiResponse<List<ProductCategoryVO>> getCategoryTree(
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId) {
        log.info("开始查询分类树，租户ID：{}", tenantId);
        
        try {
            List<ProductCategoryVO> tree = productCategoryService.getCategoryTree(tenantId);
            log.info("查询分类树成功，分类数量：{}", tree.size());
            return ApiResponse.success(tree);
        } catch (Exception e) {
            log.error("查询分类树失败，租户ID：{}", tenantId, e);
            return ApiResponse.error(500, "查询分类树失败：" + e.getMessage());
        }
    }

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @param tenantId 租户ID
     * @return 子分类列表
     */
    @GetMapping("/children")
    @Operation(summary = "查询子分类", description = "根据父分类ID查询子分类列表")
    public ApiResponse<List<ProductCategoryVO>> getCategoriesByParentId(
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId) {
        log.info("开始查询子分类，父分类ID：{}，租户ID：{}", parentId, tenantId);
        
        try {
            List<ProductCategoryVO> categories = productCategoryService.getCategoriesByParentId(parentId, tenantId);
            log.info("查询子分类成功，子分类数量：{}", categories.size());
            return ApiResponse.success(categories);
        } catch (Exception e) {
            log.error("查询子分类失败，父分类ID：{}，租户ID：{}", parentId, tenantId, e);
            return ApiResponse.error(500, "查询子分类失败：" + e.getMessage());
        }
    }

    /**
     * 检查分类标识符是否存在
     *
     * @param code 分类标识符
     * @param tenantId 租户ID
     * @param excludeId 排除的分类ID
     * @return 是否存在
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查分类标识符", description = "检查分类标识符是否已存在")
    public ApiResponse<Boolean> checkCodeExists(
            @Parameter(description = "分类标识符") @RequestParam @NotNull String code,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId,
            @Parameter(description = "排除的分类ID") @RequestParam(required = false) Long excludeId) {
        log.info("开始检查分类标识符是否存在，code：{}，tenantId：{}，excludeId：{}", code, tenantId, excludeId);
        
        try {
            boolean exists = productCategoryService.existsByCode(code, tenantId, excludeId);
            log.info("检查分类标识符完成，code：{}，存在：{}", code, exists);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("检查分类标识符失败，code：{}，tenantId：{}，excludeId：{}", code, tenantId, excludeId, e);
            return ApiResponse.error(500, "检查分类标识符失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除产品分类
     *
     * @param categoryIds 分类ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除产品分类", description = "批量删除多个产品分类")
    public ApiResponse<Void> batchDeleteCategories(
            @Parameter(description = "分类ID列表") @RequestBody @NotNull List<Long> categoryIds) {
        log.info("开始批量删除产品分类，分类ID列表：{}", categoryIds);
        
        try {
            boolean success = productCategoryService.batchDeleteCategories(categoryIds);
            if (success) {
                log.info("批量删除产品分类成功，分类ID列表：{}", categoryIds);
                return ApiResponse.success();
            } else {
                log.warn("批量删除产品分类失败，分类ID列表：{}", categoryIds);
                return ApiResponse.error(500, "批量删除产品分类失败");
            }
        } catch (Exception e) {
            log.error("批量删除产品分类失败，分类ID列表：{}", categoryIds, e);
            return ApiResponse.error(500, "批量删除产品分类失败：" + e.getMessage());
        }
    }
}

 