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
import java.util.stream.Collectors;

/**
 * 产品分类服务实现类
 *
 * @author rose
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Override
    public ProductCategoryVO createCategory(ProductCategoryCreateRequest request) {
        log.info("开始创建产品分类，请求参数：{}", request);

        // 检查分类标识符是否已存在
        if (existsByCode(request.getCode())) {
            throw new IllegalArgumentException("分类标识符已存在：" + request.getCode());
        }

        // 检查父分类是否存在
        if (request.getParentId() != null) {
            ProductCategory parentCategory = getById(request.getParentId());
            if (parentCategory == null) {
                throw new IllegalArgumentException("父分类不存在：" + request.getParentId());
            }
        }

        // 创建分类实体
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(request, category);

        // 设置分类层级
        if (request.getParentId() != null) {
            ProductCategory parentCategory = getById(request.getParentId());
            category.setLevel(parentCategory.getLevel() + 1);
        } else {
            category.setLevel(1);
        }

        // 保存分类
        save(category);

        log.info("产品分类创建成功，分类ID：{}", category.getId());

        return convertToVO(category);
    }

    @Override
    public ProductCategoryVO updateCategory(Long id, ProductCategoryUpdateRequest request) {
        log.info("开始更新产品分类，分类ID：{}，请求参数：{}", id, request);

        // 检查分类是否存在
        ProductCategory category = getById(id);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在：" + id);
        }

        // 检查分类标识符是否重复（排除自身）
        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(category.getCode())) {
            if (existsByCode(request.getCode())) {
                throw new IllegalArgumentException("分类标识符已存在：" + request.getCode());
            }
        }

        // 检查父分类是否存在且不能是自己
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("父分类不能是自己");
            }
            ProductCategory parentCategory = getById(request.getParentId());
            if (parentCategory == null) {
                throw new IllegalArgumentException("父分类不存在：" + request.getParentId());
            }
        }

        // 更新分类信息
        BeanUtils.copyProperties(request, category);

        // 更新分类层级
        if (request.getParentId() != null) {
            ProductCategory parentCategory = getById(request.getParentId());
            category.setLevel(parentCategory.getLevel() + 1);
        } else {
            category.setLevel(1);
        }

        // 保存更新
        updateById(category);

        log.info("产品分类更新成功，分类ID：{}", id);

        return convertToVO(category);
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("开始删除产品分类，分类ID：{}", id);

        // 检查分类是否存在
        ProductCategory category = getById(id);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在：" + id);
        }

        // 检查是否有子分类
        List<ProductCategory> children = baseMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("存在子分类，无法删除");
        }

        // 逻辑删除分类
        removeById(id);

        log.info("产品分类删除成功，分类ID：{}", id);
    }

    @Override
    public ProductCategoryVO getCategoryById(Long id) {
        ProductCategory category = getById(id);
        return category != null ? convertToVO(category) : null;
    }

    @Override
    public IPage<ProductCategoryVO> pageCategories(Page<ProductCategory> page, ProductCategoryQueryRequest request) {
        log.info("开始分页查询产品分类，请求参数：{}", request);

        // 构建查询条件
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getName()), ProductCategory::getName, request.getName())
                .eq(StringUtils.hasText(request.getCode()), ProductCategory::getCode, request.getCode())
                .eq(request.getParentId() != null, ProductCategory::getParentId, request.getParentId())
                .eq(request.getType() != null, ProductCategory::getType, request.getType())
                .eq(request.getStatus() != null, ProductCategory::getStatus, request.getStatus())
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId);

        // 执行分页查询
        IPage<ProductCategory> categoryPage = page(page, wrapper);

        // 转换为VO
        IPage<ProductCategoryVO> result = new Page<>();
        BeanUtils.copyProperties(categoryPage, result);
        result.setRecords(categoryPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        log.info("产品分类分页查询完成，总记录数：{}", result.getTotal());

        return result;
    }

    @Override
    public List<ProductCategoryVO> getCategoryTree(Long parentId) {
        log.info("开始查询分类树结构，父分类ID：{}", parentId);

        List<ProductCategory> categories = baseMapper.selectCategoryTree(parentId);
        List<ProductCategoryVO> result = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 递归构建树结构
        for (ProductCategoryVO category : result) {
            category.setChildren(getCategoryTree(category.getId()));
        }

        log.info("分类树结构查询完成，记录数：{}", result.size());

        return result;
    }

    @Override
    public ProductCategoryVO getCategoryByCode(String code) {
        ProductCategory category = baseMapper.selectByCode(code);
        return category != null ? convertToVO(category) : null;
    }

    @Override
    public boolean existsByCode(String code) {
        return baseMapper.existsByCode(code);
    }

    @Override
    public List<ProductCategoryVO> getCategoriesByType(ProductCategory.CategoryType type) {
        List<ProductCategory> categories = baseMapper.selectByType(type);
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCategoryVO> getCategoriesByStatus(ProductCategory.CategoryStatus status) {
        List<ProductCategory> categories = baseMapper.selectByStatus(status);
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateCategoryStatus(Long id, ProductCategory.CategoryStatus status) {
        log.info("开始更新分类状态，分类ID：{}，新状态：{}", id, status);

        // 检查分类是否存在
        ProductCategory category = getById(id);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在：" + id);
        }

        // 更新状态
        category.setStatus(status);
        updateById(category);

        log.info("分类状态更新成功，分类ID：{}，新状态：{}", id, status);
    }

    /**
     * 将实体转换为VO
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
        vo.setChildren(new ArrayList<>()); // 初始化子分类列表

        return vo;
    }
}