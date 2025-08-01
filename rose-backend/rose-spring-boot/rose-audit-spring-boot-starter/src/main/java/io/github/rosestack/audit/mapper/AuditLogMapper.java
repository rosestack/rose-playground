package io.github.rosestack.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志主表 Mapper 接口
 * <p>
 * 提供审计日志的数据访问功能，包括基础的 CRUD 操作和复杂查询。
 * 支持多租户查询、分页查询、统计查询等功能。
 * 针对大数据量场景进行了性能优化。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}