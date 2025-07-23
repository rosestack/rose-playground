package io.github.rose.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rose.device.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 产品分类数据访问接口
 * <p>
 * 提供产品分类的数据库操作，包括基础的CRUD操作和自定义查询方法。
 * 严格遵循MyBatis Plus MDC规范：
 * - 优先使用Wrapper进行查询
 * - 避免复杂SQL和多表联合查询
 * - 避免使用数据库特定函数（如递归）
 * - 只保留必要的简单SQL查询
 * </p>
 *
 * @author rose
 * @since 2024-01-01
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
   
} 