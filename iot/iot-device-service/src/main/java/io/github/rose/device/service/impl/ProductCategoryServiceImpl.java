package io.github.rose.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rose.device.dto.ProductCategoryCreateRequest;
import io.github.rose.device.dto.ProductCategoryQueryRequest;
import io.github.rose.device.dto.ProductCategoryUpdateRequest;
import io.github.rose.device.entity.ProductCategory;
import io.github.rose.device.mapper.ProductCategoryMapper;
import io.github.rose.device.service.ProductCategoryService;
import io.github.rose.device.vo.ProductCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品分类服务实现类
 * <p>
 * 提供产品分类的业务逻辑实现，包括创建、更新、删除、查询等功能。
 * 支持树形结构管理和分页查询。
 * 遵循MyBatis Plus MDC规范，优先使用Wrapper进行查询。
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements ProductCategoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryVO createCategory(ProductCategoryCreateRequest request) {
        try {
            // 检查分类标识符是否已存在
            if (existsByCode(request.getCode(), request.getTenantId(), null)) {
                throw new BusinessException("分类标识符已存在：" + request.getCode());
            }

            // 构建分类实体
            ProductCategory category = new ProductCategory();
            BeanUtils.copyProperties(request, category);

            // 设置默认值
            if (category.getLevel() == null) {
                category.setLevel(1);
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            if (category.getType() == null) {
                category.setType(ProductCategory.CategoryType.CUSTOM);
            }
            if (category.getStatus() == null) {
                category.setStatus(ProductCategory.CategoryStatus.ACTIVE);
            }

            // 如果有父分类，计算层级
            if (category.getParentId() != null) {
                ProductCategory parentCategory = this.getById(category.getParentId());
                if (parentCategory == null) {
                    throw new BusinessException("父分类不存在：" + category.getParentId());
                }
                category.setLevel(parentCategory.getLevel() + 1);
            }

            // 保存分类
            this.save(category);

            log.info("产品分类创建成功，分类ID：{}，分类名称：{}", category.getId(), category.getName());

            return convertToVO(category);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建产品分类异常，请求参数：{}", request, e);
            throw new BusinessException("创建产品分类失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryVO updateCategory(ProductCategoryUpdateRequest request) {
        try {
            // 检查分类是否存在
            ProductCategory existingCategory = this.getById(request.getId());
            if (existingCategory == null) {
                throw new BusinessException("分类不存在：" + request.getId());
            }

            // 检查分类标识符是否已存在（排除当前分类）
            if (StringUtils.hasText(request.getCode()) &&
                    existsByCode(request.getCode(), existingCategory.getTenantId(), request.getId())) {
                throw new BusinessException("分类标识符已存在：" + request.getCode());
            }

            // 更新分类信息
            BeanUtils.copyProperties(request, existingCategory);

            // 如果有父分类，计算层级
            if (existingCategory.getParentId() != null) {
                ProductCategory parentCategory = this.getById(existingCategory.getParentId());
                if (parentCategory == null) {
                    throw new BusinessException("父分类不存在：" + existingCategory.getParentId());
                }
                existingCategory.setLevel(parentCategory.getLevel() + 1);
            }

            // 保存更新
            this.updateById(existingCategory);

            log.info("产品分类更新成功，分类ID：{}，分类名称：{}", existingCategory.getId(), existingCategory.getName());

            return convertToVO(existingCategory);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新产品分类异常，请求参数：{}", request, e);
            throw new BusinessException("更新产品分类失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long categoryId) {
        try {
            // 检查分类是否存在
            ProductCategory category = this.getById(categoryId);
            if (category == null) {
                throw new BusinessException("分类不存在：" + categoryId);
            }

            // 检查是否有子分类（使用Wrapper查询）
            LambdaQueryWrapper<ProductCategory> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(ProductCategory::getParentId, categoryId)
                    .eq(ProductCategory::getDeleted, false);
            long childCount = this.count(childWrapper);
            if (childCount > 0) {
                throw new BusinessException("分类下存在子分类，无法删除");
            }

            // 检查是否有产品
            long productCount = countProductsByCategory(categoryId);
            if (productCount > 0) {
                throw new BusinessException("分类下存在产品，无法删除");
            }

            // 删除分类
            boolean result = this.removeById(categoryId);

            log.info("产品分类删除成功，分类ID：{}，分类名称：{}", categoryId, category.getName());

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除产品分类异常，分类ID：{}", categoryId, e);
            throw new BusinessException("删除产品分类失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryVO getCategoryById(Long categoryId) {
        try {
            ProductCategory category = this.getById(categoryId);
            return category != null ? convertToVO(category) : null;
        } catch (Exception e) {
            log.error("获取产品分类异常，分类ID：{}", categoryId, e);
            throw new BusinessException("获取产品分类失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<ProductCategoryVO> pageCategories(ProductCategoryQueryRequest request) {
        try {
            // 构建分页参数
            Page<ProductCategory> page = new Page<>(request.getPageNo(), request.getPageSize());

            // 使用Wrapper构建查询条件
            LambdaQueryWrapper<ProductCategory> wrapper = buildQueryWrapper(request);

            // 执行分页查询
            IPage<ProductCategory> categoryPage = this.page(page, wrapper);

            // 转换为VO
            IPage<ProductCategoryVO> voPage = new Page<>();
            BeanUtils.copyProperties(categoryPage, voPage);

            List<ProductCategoryVO> voList = categoryPage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            voPage.setRecords(voList);

            return voPage;
        } catch (Exception e) {
            log.error("分页查询产品分类异常，请求参数：{}", request, e);
            throw new BusinessException("分页查询产品分类失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryVO> getCategoryTree(Long tenantId) {
        try {
            // 使用Wrapper查询所有分类
            LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductCategory::getDeleted, false)
                    .eq(tenantId != null, ProductCategory::getTenantId, tenantId)
                    .orderByAsc(ProductCategory::getSortOrder)
                    .orderByDesc(ProductCategory::getCreatedTime);

            List<ProductCategory> allCategories = this.list(wrapper);

            // 构建分类树
            return buildCategoryTree(allCategories);
        } catch (Exception e) {
            log.error("查询分类树异常，租户ID：{}", tenantId, e);
            throw new BusinessException("查询分类树失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryVO> getCategoriesByParentId(Long parentId, Long tenantId) {
        try {
            // 使用Wrapper查询子分类
            LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductCategory::getDeleted, false)
                    .eq(parentId != null, ProductCategory::getParentId, parentId)
                    .isNull(parentId == null, ProductCategory::getParentId)
                    .eq(tenantId != null, ProductCategory::getTenantId, tenantId)
                    .orderByAsc(ProductCategory::getSortOrder)
                    .orderByDesc(ProductCategory::getCreatedTime);

            List<ProductCategory> categories = this.list(wrapper);

            return categories.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询子分类异常，父分类ID：{}，租户ID：{}", parentId, tenantId, e);
            throw new BusinessException("查询子分类失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code, Long tenantId, Long excludeId) {
        try {
            LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductCategory::getCode, code)
                    .eq(ProductCategory::getTenantId, tenantId)
                    .eq(ProductCategory::getDeleted, false);

            if (excludeId != null) {
                wrapper.ne(ProductCategory::getId, excludeId);
            }

            return this.count(wrapper) > 0;
        } catch (Exception e) {
            log.error("检查分类标识符异常，code：{}，tenantId：{}，excludeId：{}", code, tenantId, excludeId, e);
            throw new BusinessException("检查分类标识符失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteCategories(List<Long> categoryIds) {
        try {
            for (Long categoryId : categoryIds) {
                deleteCategory(categoryId);
            }

            log.info("批量删除产品分类成功，分类ID列表：{}", categoryIds);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除产品分类异常，分类ID列表：{}", categoryIds, e);
            throw new BusinessException("批量删除产品分类失败：" + e.getMessage());
        }
    }

    /**
     * 构建查询条件Wrapper
     *
     * @param request 查询请求
     * @return 查询条件Wrapper
     */
    private LambdaQueryWrapper<ProductCategory> buildQueryWrapper(ProductCategoryQueryRequest request) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ProductCategory::getDeleted, false)
                .eq(request.getParentId() != null, ProductCategory::getParentId, request.getParentId())
                .like(StringUtils.hasText(request.getName()), ProductCategory::getName, request.getName())
                .like(StringUtils.hasText(request.getCode()), ProductCategory::getCode, request.getCode())
                .eq(request.getType() != null, ProductCategory::getType, request.getType())
                .eq(request.getStatus() != null, ProductCategory::getStatus, request.getStatus())
                .eq(request.getTenantId() != null, ProductCategory::getTenantId, request.getTenantId())
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByDesc(ProductCategory::getCreatedTime);

        return wrapper;
    }

    /**
     * 统计分类下的产品数量
     *
     * @param categoryId 分类ID
     * @return 产品数量
     */
    private long countProductsByCategory(Long categoryId) {
        try {
            return baseMapper.selectCount(Wrappers.lambdaQuery(ProductCategory.class).eq(ProductCategory::getId, categoryId));
        } catch (Exception e) {
            log.warn("统计产品数量失败，分类ID：{}", categoryId, e);
            return 0;
        }
    }

    /**
     * 构建分类树
     *
     * @param categories 所有分类列表
     * @return 分类树
     */
    private List<ProductCategoryVO> buildCategoryTree(List<ProductCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        // 按父分类ID分组
        Map<Long, List<ProductCategory>> parentMap = categories.stream()
                .collect(Collectors.groupingBy(category ->
                        category.getParentId() == null ? 0L : category.getParentId()));

        // 递归构建树
        return buildTreeRecursive(0L, parentMap);
    }

    /**
     * 递归构建分类树
     *
     * @param parentId  父分类ID
     * @param parentMap 父分类映射
     * @return 子分类列表
     */
    private List<ProductCategoryVO> buildTreeRecursive(Long parentId, Map<Long, List<ProductCategory>> parentMap) {
        List<ProductCategory> children = parentMap.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }

        return children.stream()
                .map(category -> {
                    ProductCategoryVO vo = convertToVO(category);
                    vo.setChildren(buildTreeRecursive(category.getId(), parentMap));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     *
     * @param category 分类实体
     * @return 分类VO
     */
    private ProductCategoryVO convertToVO(ProductCategory category) {
        if (category == null) {
            return null;
        }

        ProductCategoryVO vo = new ProductCategoryVO();
        BeanUtils.copyProperties(category, vo);

        // 统计产品数量
        try {
            long productCount = countProductsByCategory(category.getId());
            vo.setProductCount(productCount);
        } catch (Exception e) {
            log.warn("统计产品数量失败，分类ID：{}", category.getId(), e);
            vo.setProductCount(0L);
        }

        return vo;
    }

    /**
     * 业务异常类
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }

        public BusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 