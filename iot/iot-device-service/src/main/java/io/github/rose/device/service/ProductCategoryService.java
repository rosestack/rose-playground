package io.github.rose.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rose.device.dto.ProductCategoryCreateRequest;
import io.github.rose.device.dto.ProductCategoryQueryRequest;
import io.github.rose.device.dto.ProductCategoryUpdateRequest;
import io.github.rose.device.entity.ProductCategory;
import io.github.rose.device.vo.ProductCategoryVO;

import java.util.List;

/**
 * 产品分类服务接口
 *
 * @author rose
 * @since 2024-01-01
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 创建产品分类
     *
     * @param request 创建请求
     * @return 创建的分类信息
     */
    ProductCategoryVO createCategory(ProductCategoryCreateRequest request);

    /**
     * 更新产品分类
     *
     * @param request 更新请求
     * @return 更新后的分类信息
     */
    ProductCategoryVO updateCategory(ProductCategoryUpdateRequest request);

    /**
     * 删除产品分类
     *
     * @param categoryId 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Long categoryId);

    /**
     * 根据ID获取产品分类
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    ProductCategoryVO getCategoryById(Long categoryId);

    /**
     * 分页查询产品分类
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<ProductCategoryVO> pageCategories(ProductCategoryQueryRequest request);

    /**
     * 查询分类树
     *
     * @param tenantId 租户ID
     * @return 分类树列表
     */
    List<ProductCategoryVO> getCategoryTree(Long tenantId);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @param tenantId 租户ID
     * @return 子分类列表
     */
    List<ProductCategoryVO> getCategoriesByParentId(Long parentId, Long tenantId);

    /**
     * 检查分类标识符是否存在
     *
     * @param code 分类标识符
     * @param tenantId 租户ID
     * @param excludeId 排除的分类ID
     * @return 是否存在
     */
    boolean existsByCode(String code, Long tenantId, Long excludeId);

    /**
     * 批量删除产品分类
     *
     * @param categoryIds 分类ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteCategories(List<Long> categoryIds);
} 