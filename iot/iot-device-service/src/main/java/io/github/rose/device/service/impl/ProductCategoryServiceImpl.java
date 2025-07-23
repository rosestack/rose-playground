package io.github.rose.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
 *
 * @author rose
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements ProductCategoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryVO createCategory(ProductCategoryCreateRequest request) {
        log.info("创建产品分类，请求参数：{}", request);

        // 检查分类标识符是否已存在
        if (existsByCode(request.getCode(), request.getTenantId(), null)) {
            throw new RuntimeException("分类标识符已存在：" + request.getCode());
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
                throw new RuntimeException("父分类不存在：" + category.getParentId());
            }
            category.setLevel(parentCategory.getLevel() + 1);
        }

        // 保存分类
        this.save(category);

        log.info("产品分类创建成功，分类ID：{}", category.getId());

        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryVO updateCategory(ProductCategoryUpdateRequest request) {
        log.info("更新产品分类，请求参数：{}", request);

        // 检查分类是否存在
        ProductCategory existingCategory = this.getById(request.getId());
        if (existingCategory == null) {
            throw new RuntimeException("分类不存在：" + request.getId());
        }

        // 检查分类标识符是否已存在（排除当前分类）
        if (StringUtils.hasText(request.getCode()) &&
                existsByCode(request.getCode(), existingCategory.getTenantId(), request.getId())) {
            throw new RuntimeException("分类标识符已存在：" + request.getCode());
        }

        // 更新分类信息
        BeanUtils.copyProperties(request, existingCategory);

        // 如果有父分类，计算层级
        if (existingCategory.getParentId() != null) {
            ProductCategory parentCategory = this.getById(existingCategory.getParentId());
            if (parentCategory == null) {
                throw new RuntimeException("父分类不存在：" + existingCategory.getParentId());
            }
            existingCategory.setLevel(parentCategory.getLevel() + 1);
        }

        // 保存更新
        this.updateById(existingCategory);

        log.info("产品分类更新成功，分类ID：{}", existingCategory.getId());

        return convertToVO(existingCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long categoryId) {
        log.info("删除产品分类，分类ID：{}", categoryId);

        // 检查分类是否存在
        ProductCategory category = this.getById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在：" + categoryId);
        }

        // 检查是否有子分类
        List<Long> childIds = baseMapper.selectChildIds(categoryId);
        if (!childIds.isEmpty()) {
            throw new RuntimeException("分类下存在子分类，无法删除");
        }

        // 检查是否有产品
        int productCount = baseMapper.countProductsByCategory(categoryId);
        if (productCount > 0) {
            throw new RuntimeException("分类下存在产品，无法删除");
        }

        // 删除分类
        boolean result = this.removeById(categoryId);

        log.info("产品分类删除成功，分类ID：{}", categoryId);

        return result;
    }

    @Override
    public ProductCategoryVO getCategoryById(Long categoryId) {
        log.info("根据ID获取产品分类，分类ID：{}", categoryId);

        ProductCategory category = this.getById(categoryId);
        if (category == null) {
            return null;
        }

        return convertToVO(category);
    }

    @Override
    public IPage<ProductCategoryVO> pageCategories(ProductCategoryQueryRequest request) {
        log.info("分页查询产品分类，请求参数：{}", request);

        // 构建分页参数
        Page<ProductCategory> page = new Page<>(request.getPageNo(), request.getPageSize());

        // 执行分页查询
        IPage<ProductCategory> categoryPage = baseMapper.selectPage(page,
                request.getParentId(),
                request.getName(),
                request.getCode(),
                request.getType(),
                request.getStatus(),
                request.getTenantId());

        // 转换为VO
        IPage<ProductCategoryVO> voPage = new Page<>();
        BeanUtils.copyProperties(categoryPage, voPage);

        List<ProductCategoryVO> voList = categoryPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<ProductCategoryVO> getCategoryTree(Long tenantId) {
        log.info("查询分类树，租户ID：{}", tenantId);

        // 查询所有分类
        List<ProductCategory> allCategories = baseMapper.selectCategoryTree(tenantId);

        // 构建分类树
        return buildCategoryTree(allCategories);
    }

    @Override
    public List<ProductCategoryVO> getCategoriesByParentId(Long parentId, Long tenantId) {
        log.info("根据父分类ID查询子分类，父分类ID：{}，租户ID：{}", parentId, tenantId);

        List<ProductCategory> categories = baseMapper.selectByParentId(parentId, tenantId);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code, Long tenantId, Long excludeId) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getCode, code)
                .eq(ProductCategory::getTenantId, tenantId)
                .eq(ProductCategory::getDeleted, false);

        if (excludeId != null) {
            wrapper.ne(ProductCategory::getId, excludeId);
        }

        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteCategories(List<Long> categoryIds) {
        log.info("批量删除产品分类，分类ID列表：{}", categoryIds);

        for (Long categoryId : categoryIds) {
            deleteCategory(categoryId);
        }

        return true;
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
        int productCount = baseMapper.countProductsByCategory(category.getId());
        vo.setProductCount(productCount);

        return vo;
    }
} 