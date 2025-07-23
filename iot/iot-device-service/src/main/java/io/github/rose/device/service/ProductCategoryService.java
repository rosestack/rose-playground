package io.github.rose.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rose.device.dto.ProductCategoryCreateRequest;
import io.github.rose.device.dto.ProductCategoryQueryRequest;
import io.github.rose.device.dto.ProductCategoryUpdateRequest;
import io.github.rose.device.entity.ProductCategory;
import io.github.rose.device.vo.ProductCategoryVO;

import java.util.List;

/**
 * 产品分类服务接口
 * <p>
 * 提供产品分类的业务逻辑处理，包括分类的增删改查、树结构管理等。
 * </p>
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
     * @param id      分类ID
     * @param request 更新请求
     * @return 更新后的分类信息
     */
    ProductCategoryVO updateCategory(Long id, ProductCategoryUpdateRequest request);

    /**
     * 删除产品分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 根据ID获取产品分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    ProductCategoryVO getCategoryById(Long id);

    /**
     * 分页查询产品分类
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<ProductCategoryVO> pageCategories(Page<ProductCategory>  page,ProductCategoryQueryRequest request);

    /**
     * 查询分类树结构
     *
     * @param parentId 父分类ID，为null时查询根分类
     * @return 分类树列表
     */
    List<ProductCategoryVO> getCategoryTree(Long parentId);

    /**
     * 根据分类标识符获取分类
     *
     * @param code 分类标识符
     * @return 分类信息
     */
    ProductCategoryVO getCategoryByCode(String code);

    /**
     * 检查分类标识符是否存在
     *
     * @param code 分类标识符
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 根据分类类型查询分类列表
     *
     * @param type 分类类型
     * @return 分类列表
     */
    List<ProductCategoryVO> getCategoriesByType(ProductCategory.CategoryType type);

    /**
     * 根据分类状态查询分类列表
     *
     * @param status 分类状态
     * @return 分类列表
     */
    List<ProductCategoryVO> getCategoriesByStatus(ProductCategory.CategoryStatus status);

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 新状态
     */
    void updateCategoryStatus(Long id, ProductCategory.CategoryStatus status);
} 