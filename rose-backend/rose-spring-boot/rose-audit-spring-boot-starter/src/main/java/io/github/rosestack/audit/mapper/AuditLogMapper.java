package io.github.rosestack.audit.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rosestack.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 根据用户ID查询审计日志
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    default List<AuditLog> selectByUserId(String userId) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getUserId, userId)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据事件类型查询审计日志
     *
     * @param eventType 事件类型
     * @return 审计日志列表
     */
    default List<AuditLog> selectByEventType(String eventType) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getEventType, eventType)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据风险等级查询审计日志
     *
     * @param riskLevel 风险等级
     * @return 审计日志列表
     */
    default List<AuditLog> selectByRiskLevel(String riskLevel) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getRiskLevel, riskLevel)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据操作状态查询审计日志
     *
     * @param status 操作状态
     * @return 审计日志列表
     */
    default List<AuditLog> selectByStatus(String status) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getStatus, status)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据时间范围查询审计日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审计日志列表
     */
    default List<AuditLog> selectByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据时间范围分页查询审计日志
     *
     * @param page      分页参数
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    default IPage<AuditLog> selectPageByTimeRange(Page<AuditLog> page, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectPage(page, wrapper);
    }

    // ==================== 复合条件查询 ====================

    /**
     * 根据用户和时间范围查询审计日志
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审计日志列表
     */
    default List<AuditLog> selectByUserAndTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getUserId, userId)
                .between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据事件类型和时间范围查询审计日志
     *
     * @param eventType 事件类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审计日志列表
     */
    default List<AuditLog> selectByEventTypeAndTimeRange(String eventType, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getEventType, eventType)
                .between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据多个条件分页查询审计日志
     *
     * @param page      分页参数
     * @param userId    用户ID（可选）
     * @param eventType 事件类型（可选）
     * @param riskLevel 风险等级（可选）
     * @param status    操作状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 分页结果
     */
    default IPage<AuditLog> selectPageByConditions(Page<AuditLog> page, String userId, String eventType,
                                                   String riskLevel, String status,
                                                   LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null && !userId.trim().isEmpty()) {
            wrapper.eq(AuditLog::getUserId, userId);
        }
        if (eventType != null && !eventType.trim().isEmpty()) {
            wrapper.eq(AuditLog::getEventType, eventType);
        }
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            wrapper.eq(AuditLog::getRiskLevel, riskLevel);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(AuditLog::getStatus, status);
        }
        if (startTime != null && endTime != null) {
            wrapper.between(AuditLog::getEventTime, startTime, endTime);
        } else if (startTime != null) {
            wrapper.ge(AuditLog::getEventTime, startTime);
        } else if (endTime != null) {
            wrapper.le(AuditLog::getEventTime, endTime);
        }

        wrapper.orderByDesc(AuditLog::getEventTime);
        return selectPage(page, wrapper);
    }

    // ==================== 统计查询 ====================

    /**
     * 统计指定时间范围内的审计日志数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志数量
     */
    @Select("SELECT COUNT(*) FROM audit_log WHERE event_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0")
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定用户的审计日志数量
     *
     * @param userId 用户ID
     * @return 日志数量
     */
    @Select("SELECT COUNT(*) FROM audit_log WHERE user_id = #{userId} AND deleted = 0")
    Long countByUserId(@Param("userId") String userId);

    /**
     * 统计各事件类型的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果 Map<事件类型, 数量>
     */
    @Select("SELECT event_type, COUNT(*) as count FROM audit_log " +
            "WHERE event_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY event_type ORDER BY count DESC")
    List<Map<String, Object>> countByEventType(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各风险等级的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果 Map<风险等级, 数量>
     */
    @Select("SELECT risk_level, COUNT(*) as count FROM audit_log " +
            "WHERE event_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY risk_level ORDER BY count DESC")
    List<Map<String, Object>> countByRiskLevel(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各操作状态的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果 Map<操作状态, 数量>
     */
    @Select("SELECT status, COUNT(*) as count FROM audit_log " +
            "WHERE event_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY status ORDER BY count DESC")
    List<Map<String, Object>> countByStatus(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询高风险审计日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 高风险日志列表
     */
    default List<AuditLog> selectHighRiskLogs(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AuditLog::getRiskLevel, "HIGH", "CRITICAL")
                .between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 查询失败的操作日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 失败操作日志列表
     */
    default List<AuditLog> selectFailedOperations(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AuditLog::getStatus, "FAILURE", "TIMEOUT", "CANCELLED", "DENIED")
                .between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 查询安全事件日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 安全事件日志列表
     */
    default List<AuditLog> selectSecurityEvents(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getEventType, "安全")
                .between(AuditLog::getEventTime, startTime, endTime)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 查询过期的审计日志ID列表（用于批量删除）
     *
     * @param expireTime 过期时间
     * @param limit      限制数量
     * @return 过期日志ID列表
     */
    @Select("SELECT id FROM audit_log WHERE created_time < #{expireTime} AND deleted = 0 LIMIT #{limit}")
    List<Long> selectExpiredLogIds(@Param("expireTime") LocalDateTime expireTime, @Param("limit") int limit);

    /**
     * 统计过期的审计日志数量
     *
     * @param expireTime 过期时间
     * @return 过期日志数量
     */
    @Select("SELECT COUNT(*) FROM audit_log WHERE created_time < #{expireTime} AND deleted = 0")
    Long countExpiredLogs(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 根据租户ID查询审计日志
     *
     * @param tenantId 租户ID
     * @return 审计日志列表
     */
    default List<AuditLog> selectByTenantId(String tenantId) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTenantId, tenantId)
                .orderByDesc(AuditLog::getEventTime);
        return selectList(wrapper);
    }

    /**
     * 根据租户ID分页查询审计日志
     *
     * @param page     分页参数
     * @param tenantId 租户ID
     * @return 分页结果
     */
    default IPage<AuditLog> selectPageByTenantId(Page<AuditLog> page, String tenantId) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTenantId, tenantId)
                .orderByDesc(AuditLog::getEventTime);
        return selectPage(page, wrapper);
    }

    /**
     * 统计租户的审计日志数量
     *
     * @param tenantId 租户ID
     * @return 日志数量
     */
    @Select("SELECT COUNT(*) FROM audit_log WHERE tenant_id = #{tenantId} AND deleted = 0")
    Long countByTenantId(@Param("tenantId") String tenantId);
}