package io.github.rose.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.device.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品分类数据访问接口
 * <p>
 * 提供产品分类的数据库操作，包括基础的CRUD操作和自定义查询方法。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 根据父分类ID查询子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    default List<ProductCategory> selectByParentId(Long parentId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, parentId)
                .eq(ProductCategory::getDeleted, false)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId));
    }

    /**
     * 根据分类标识符查询分类
     *
     * @param code 分类标识符
     * @return 分类信息
     */
    default ProductCategory selectByCode(String code) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCode, code)
                .eq(ProductCategory::getDeleted, false));
    }

    /**
     * 检查分类标识符是否存在
     *
     * @param code 分类标识符
     * @return 是否存在
     */
    default boolean existsByCode(String code) {
        return exists(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCode, code)
                .eq(ProductCategory::getDeleted, false));
    }

    /**
     * 根据分类类型查询分类列表
     *
     * @param type 分类类型
     * @return 分类列表
     */
    default List<ProductCategory> selectByType(ProductCategory.CategoryType type) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getType, type)
                .eq(ProductCategory::getDeleted, false)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId));
    }

    /**
     * 根据分类状态查询分类列表
     *
     * @param status 分类状态
     * @return 分类列表
     */
    default List<ProductCategory> selectByStatus(ProductCategory.CategoryStatus status) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, status)
                .eq(ProductCategory::getDeleted, false)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId));
    }

    /**
     * 查询分类树结构
     *
     * @param parentId 父分类ID，为null时查询根分类
     * @return 分类树列表
     */
    default List<ProductCategory> selectCategoryTree(Long parentId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(parentId != null, ProductCategory::getParentId, parentId)
                .isNull(parentId == null, ProductCategory::getParentId)
                .eq(ProductCategory::getDeleted, false)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId));
    }

    /**
     * 根据租户ID查询分类列表
     *
     * @param tenantId 租户ID
     * @return 分类列表
     */
    default List<ProductCategory> selectByTenantId(Long tenantId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getTenantId, tenantId)
                .eq(ProductCategory::getDeleted, false)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId));
    }
} 