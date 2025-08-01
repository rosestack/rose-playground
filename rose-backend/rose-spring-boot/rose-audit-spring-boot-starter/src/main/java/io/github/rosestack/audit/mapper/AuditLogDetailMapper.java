package io.github.rosestack.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.audit.entity.AuditLogDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志详情表 Mapper 接口
 * <p>
 * 提供审计日志详情的数据访问功能，包括基础的 CRUD 操作和复杂查询。
 * 支持按审计日志ID、详情类型、详情键等多种方式查询。
 * 针对大数据量场景进行了性能优化。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface AuditLogDetailMapper extends BaseMapper<AuditLogDetail> {

}