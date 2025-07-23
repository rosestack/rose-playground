package io.github.rose.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rose.device.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品分类Mapper接口
 *
 * @author rose
 * @since 2024-01-01
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 分页查询产品分类
     *
     * @param page 分页参数
     * @param parentId 父分类ID
     * @param name 分类名称（模糊查询）
     * @param code 分类标识符（模糊查询）
     * @param type 分类类型
     * @param status 分类状态
     * @param tenantId 租户ID
     * @return 分页结果
     */
    IPage<ProductCategory> selectPage(Page<ProductCategory> page,
                                    @Param("parentId") Long parentId,
                                    @Param("name") String name,
                                    @Param("code") String code,
                                    @Param("type") String type,
                                    @Param("status") String status,
                                    @Param("tenantId") Long tenantId);

    /**
     * 查询分类树
     *
     * @param tenantId 租户ID
     * @return 分类树列表
     */
    List<ProductCategory> selectCategoryTree(@Param("tenantId") Long tenantId);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @param tenantId 租户ID
     * @return 子分类列表
     */
    List<ProductCategory> selectByParentId(@Param("parentId") Long parentId,
                                          @Param("tenantId") Long tenantId);

    /**
     * 查询分类的所有子分类ID
     *
     * @param categoryId 分类ID
     * @return 子分类ID列表
     */
    List<Long> selectChildIds(@Param("categoryId") Long categoryId);

    /**
     * 查询分类的所有父分类ID
     *
     * @param categoryId 分类ID
     * @return 父分类ID列表
     */
    List<Long> selectParentIds(@Param("categoryId") Long categoryId);

    /**
     * 检查分类标识符是否存在
     *
     * @param code 分类标识符
     * @param tenantId 租户ID
     * @param excludeId 排除的分类ID
     * @return 是否存在
     */
    boolean existsByCode(@Param("code") String code,
                        @Param("tenantId") Long tenantId,
                        @Param("excludeId") Long excludeId);

    /**
     * 统计分类下的产品数量
     *
     * @param categoryId 分类ID
     * @return 产品数量
     */
    int countProductsByCategory(@Param("categoryId") Long categoryId);
} 