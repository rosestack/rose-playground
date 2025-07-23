package io.github.rose.device.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rose.device.dto.ProductCategoryCreateRequest;
import io.github.rose.device.dto.ProductCategoryQueryRequest;
import io.github.rose.device.dto.ProductCategoryUpdateRequest;
import io.github.rose.device.entity.ProductCategory;
import io.github.rose.device.model.ApiResponse;
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
 * 提供产品分类的RESTful API接口，包括分类的增删改查、树结构查询等功能。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/product/categories")
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
        log.info("接收到创建产品分类请求：{}", request);

        try {
            ProductCategoryVO category = productCategoryService.createCategory(request);
            return ApiResponse.success(category);
        } catch (Exception e) {
            log.error("创建产品分类失败，请求参数：{}", request, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新产品分类
     *
     * @param id      分类ID
     * @param request 更新请求
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产品分类", description = "根据ID更新产品分类信息")
    public ApiResponse<ProductCategoryVO> updateCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable @NotNull Long id,
            @Valid @RequestBody ProductCategoryUpdateRequest request) {
        log.info("接收到更新产品分类请求，分类ID：{}，请求参数：{}", id, request);

        try {
            ProductCategoryVO category = productCategoryService.updateCategory(id, request);
            return ApiResponse.success(category);
        } catch (Exception e) {
            log.error("更新产品分类失败，分类ID：{}，请求参数：{}", id, request, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除产品分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产品分类", description = "根据ID删除产品分类")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable @NotNull Long id) {
        log.info("接收到删除产品分类请求，分类ID：{}", id);

        try {
            productCategoryService.deleteCategory(id);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("删除产品分类失败，分类ID：{}", id, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取产品分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取产品分类", description = "根据ID获取产品分类详细信息")
    public ApiResponse<ProductCategoryVO> getCategoryById(
            @Parameter(description = "分类ID", example = "1") @PathVariable @NotNull Long id) {
        log.info("接收到获取产品分类请求，分类ID：{}", id);

        try {
            ProductCategoryVO category = productCategoryService.getCategoryById(id);
            if (category == null) {
                return ApiResponse.error(404, "分类不存在");
            }
            return ApiResponse.success(category);
        } catch (Exception e) {
            log.error("获取产品分类失败，分类ID：{}", id, e);
            return ApiResponse.error(e.getMessage());
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
    public ApiResponse<IPage<ProductCategoryVO>> pageCategories(Page<ProductCategory> page, ProductCategoryQueryRequest request) {
        log.info("接收到分页查询产品分类请求：{}", request);

        try {
            IPage<ProductCategoryVO> result = productCategoryService.pageCategories(page, request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("分页查询产品分类失败，请求参数：{}", request, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询分类树结构
     *
     * @param parentId 父分类ID，为null时查询根分类
     * @return 分类树列表
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分类树结构", description = "查询产品分类的树形结构")
    public ApiResponse<List<ProductCategoryVO>> getCategoryTree(
            @Parameter(description = "父分类ID，为空时查询根分类", example = "1") @RequestParam(required = false) Long parentId) {
        log.info("接收到查询分类树结构请求，父分类ID：{}", parentId);

        try {
            List<ProductCategoryVO> tree = productCategoryService.getCategoryTree(parentId);
            return ApiResponse.success(tree);
        } catch (Exception e) {
            log.error("查询分类树结构失败，父分类ID：{}", parentId, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据分类标识符获取分类
     *
     * @param code 分类标识符
     * @return 分类信息
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据标识符获取分类", description = "根据分类标识符获取分类详细信息")
    public ApiResponse<ProductCategoryVO> getCategoryByCode(
            @Parameter(description = "分类标识符", example = "smart_home") @PathVariable @NotNull String code) {
        log.info("接收到根据标识符获取分类请求，分类标识符：{}", code);

        try {
            ProductCategoryVO category = productCategoryService.getCategoryByCode(code);
            if (category == null) {
                return ApiResponse.error(404, "分类不存在");
            }
            return ApiResponse.success(category);
        } catch (Exception e) {
            log.error("根据标识符获取分类失败，分类标识符：{}", code, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查分类标识符是否存在
     *
     * @param code 分类标识符
     * @return 是否存在
     */
    @GetMapping("/exists/{code}")
    @Operation(summary = "检查分类标识符是否存在", description = "检查指定的分类标识符是否已存在")
    public ApiResponse<Boolean> existsByCode(
            @Parameter(description = "分类标识符", example = "smart_home") @PathVariable @NotNull String code) {
        log.info("接收到检查分类标识符是否存在请求，分类标识符：{}", code);

        try {
            boolean exists = productCategoryService.existsByCode(code);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("检查分类标识符是否存在失败，分类标识符：{}", code, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据分类类型查询分类列表
     *
     * @param type 分类类型
     * @return 分类列表
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型查询分类", description = "根据分类类型查询分类列表")
    public ApiResponse<List<ProductCategoryVO>> getCategoriesByType(
            @Parameter(description = "分类类型", example = "CUSTOM") @PathVariable @NotNull ProductCategory.CategoryType type) {
        log.info("接收到根据类型查询分类请求，分类类型：{}", type);

        try {
            List<ProductCategoryVO> categories = productCategoryService.getCategoriesByType(type);
            return ApiResponse.success(categories);
        } catch (Exception e) {
            log.error("根据类型查询分类失败，分类类型：{}", type, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据分类状态查询分类列表
     *
     * @param status 分类状态
     * @return 分类列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询分类", description = "根据分类状态查询分类列表")
    public ApiResponse<List<ProductCategoryVO>> getCategoriesByStatus(
            @Parameter(description = "分类状态", example = "ACTIVE") @PathVariable @NotNull ProductCategory.CategoryStatus status) {
        log.info("接收到根据状态查询分类请求，分类状态：{}", status);

        try {
            List<ProductCategoryVO> categories = productCategoryService.getCategoriesByStatus(status);
            return ApiResponse.success(categories);
        } catch (Exception e) {
            log.error("根据状态查询分类失败，分类状态：{}", status, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新分类状态", description = "更新产品分类的状态")
    public ApiResponse<Void> updateCategoryStatus(
            @Parameter(description = "分类ID", example = "1") @PathVariable @NotNull Long id,
            @Parameter(description = "新状态", example = "ACTIVE") @RequestParam @NotNull ProductCategory.CategoryStatus status) {
        log.info("接收到更新分类状态请求，分类ID：{}，新状态：{}", id, status);

        try {
            productCategoryService.updateCategoryStatus(id, status);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("更新分类状态失败，分类ID：{}，新状态：{}", id, status, e);
            return ApiResponse.error(e.getMessage());
        }
    }
} 