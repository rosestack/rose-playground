package io.github.rosestack.audit.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rosestack.audit.entity.AuditLogDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    // ==================== 基础查询方法 ====================

    /**
     * 根据审计日志ID查询所有详情
     *
     * @param auditLogId 审计日志ID
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByAuditLogId(Long auditLogId) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getAuditLogId, auditLogId)
                .orderByAsc(AuditLogDetail::getDetailType)
                .orderByAsc(AuditLogDetail::getDetailKey);
        return selectList(wrapper);
    }

    /**
     * 根据详情类型查询详情
     *
     * @param detailType 详情类型
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByDetailType(String detailType) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getDetailType, detailType)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据详情键查询详情
     *
     * @param detailKey 详情键
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByDetailKey(String detailKey) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getDetailKey, detailKey)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据审计日志ID和详情类型查询详情
     *
     * @param auditLogId 审计日志ID
     * @param detailType 详情类型
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByAuditLogIdAndDetailType(Long auditLogId, String detailType) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getAuditLogId, auditLogId)
                .eq(AuditLogDetail::getDetailType, detailType)
                .orderByAsc(AuditLogDetail::getDetailKey);
        return selectList(wrapper);
    }

    /**
     * 根据审计日志ID和详情键查询单个详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @return 详情对象
     */
    default AuditLogDetail selectByAuditLogIdAndDetailKey(Long auditLogId, String detailKey) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getAuditLogId, auditLogId)
                .eq(AuditLogDetail::getDetailKey, detailKey)
                .last("LIMIT 1");
        return selectOne(wrapper);
    }

    // ==================== 批量查询方法 ====================

    /**
     * 根据多个审计日志ID批量查询详情
     *
     * @param auditLogIds 审计日志ID列表
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByAuditLogIds(List<Long> auditLogIds) {
        if (auditLogIds == null || auditLogIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AuditLogDetail::getAuditLogId, auditLogIds)
                .orderByAsc(AuditLogDetail::getAuditLogId)
                .orderByAsc(AuditLogDetail::getDetailType)
                .orderByAsc(AuditLogDetail::getDetailKey);
        return selectList(wrapper);
    }

    /**
     * 根据多个详情类型查询详情
     *
     * @param detailTypes 详情类型列表
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByDetailTypes(List<String> detailTypes) {
        if (detailTypes == null || detailTypes.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AuditLogDetail::getDetailType, detailTypes)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据多个详情键查询详情
     *
     * @param detailKeys 详情键列表
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByDetailKeys(List<String> detailKeys) {
        if (detailKeys == null || detailKeys.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AuditLogDetail::getDetailKey, detailKeys)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    // ==================== 敏感数据查询 ====================

    /**
     * 查询包含敏感数据的详情
     *
     * @return 敏感详情列表
     */
    default List<AuditLogDetail> selectSensitiveDetails() {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getIsSensitive, true)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 查询敏感但未加密的详情（用于数据安全检查）
     *
     * @return 敏感但未加密的详情列表
     */
    default List<AuditLogDetail> selectSensitiveButNotEncrypted() {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getIsSensitive, true)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    // ==================== 分页查询方法 ====================

    /**
     * 根据审计日志ID分页查询详情
     *
     * @param page       分页参数
     * @param auditLogId 审计日志ID
     * @return 分页结果
     */
    default IPage<AuditLogDetail> selectPageByAuditLogId(Page<AuditLogDetail> page, Long auditLogId) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getAuditLogId, auditLogId)
                .orderByAsc(AuditLogDetail::getDetailType)
                .orderByAsc(AuditLogDetail::getDetailKey);
        return selectPage(page, wrapper);
    }

    /**
     * 根据详情类型分页查询详情
     *
     * @param page       分页参数
     * @param detailType 详情类型
     * @return 分页结果
     */
    default IPage<AuditLogDetail> selectPageByDetailType(Page<AuditLogDetail> page, String detailType) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getDetailType, detailType)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectPage(page, wrapper);
    }

    // ==================== 统计查询方法 ====================

    /**
     * 统计指定审计日志的详情数量
     *
     * @param auditLogId 审计日志ID
     * @return 详情数量
     */
    @Select("SELECT COUNT(*) FROM audit_log_detail WHERE audit_log_id = #{auditLogId}")
    Long countByAuditLogId(@Param("auditLogId") Long auditLogId);

    /**
     * 统计各详情类型的数量
     *
     * @return 统计结果 Map<详情类型, 数量>
     */
    @Select("SELECT detail_type, COUNT(*) as count FROM audit_log_detail " +
            "GROUP BY detail_type ORDER BY count DESC")
    List<Map<String, Object>> countByDetailType();

    /**
     * 统计各详情键的数量
     *
     * @return 统计结果 Map<详情键, 数量>
     */
    @Select("SELECT detail_key, COUNT(*) as count FROM audit_log_detail " +
            "GROUP BY detail_key ORDER BY count DESC")
    List<Map<String, Object>> countByDetailKey();

    /**
     * 统计敏感数据的数量
     *
     * @return 敏感数据数量
     */
    @Select("SELECT COUNT(*) FROM audit_log_detail WHERE is_sensitive = 1")
    Long countSensitiveDetails();

    /**
     * 统计已加密数据的数量
     *
     * @return 已加密数据数量
     */
    @Select("SELECT COUNT(*) FROM audit_log_detail WHERE is_encrypted = 1")
    Long countEncryptedDetails();

    // ==================== 时间范围查询 ====================

    /**
     * 根据时间范围查询详情
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AuditLogDetail::getCreatedAt, startTime, endTime)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据时间范围分页查询详情
     *
     * @param page      分页参数
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    default IPage<AuditLogDetail> selectPageByTimeRange(Page<AuditLogDetail> page, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AuditLogDetail::getCreatedAt, startTime, endTime)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectPage(page, wrapper);
    }

    // ==================== 数据清理相关 ====================

    /**
     * 根据审计日志ID列表批量删除详情
     *
     * @param auditLogIds 审计日志ID列表
     * @return 删除的记录数
     */
    @Select("<script>" +
            "DELETE FROM audit_log_detail WHERE audit_log_id IN " +
            "<foreach collection='auditLogIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteByAuditLogIds(@Param("auditLogIds") List<Long> auditLogIds);

    /**
     * 查询过期的详情ID列表（用于批量删除）
     *
     * @param expireTime 过期时间
     * @param limit      限制数量
     * @return 过期详情ID列表
     */
    @Select("SELECT id FROM audit_log_detail WHERE created_at < #{expireTime} LIMIT #{limit}")
    List<Long> selectExpiredDetailIds(@Param("expireTime") LocalDateTime expireTime, @Param("limit") int limit);

    /**
     * 统计过期的详情数量
     *
     * @param expireTime 过期时间
     * @return 过期详情数量
     */
    @Select("SELECT COUNT(*) FROM audit_log_detail WHERE created_at < #{expireTime}")
    Long countExpiredDetails(@Param("expireTime") LocalDateTime expireTime);

    // ==================== 租户相关查询 ====================

    /**
     * 根据租户ID查询详情
     *
     * @param tenantId 租户ID
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByTenantId(String tenantId) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getTenantId, tenantId)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据租户ID分页查询详情
     *
     * @param page     分页参数
     * @param tenantId 租户ID
     * @return 分页结果
     */
    default IPage<AuditLogDetail> selectPageByTenantId(Page<AuditLogDetail> page, String tenantId) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDetail::getTenantId, tenantId)
                .orderByDesc(AuditLogDetail::getCreatedAt);
        return selectPage(page, wrapper);
    }

    /**
     * 统计租户的详情数量
     *
     * @param tenantId 租户ID
     * @return 详情数量
     */
    @Select("SELECT COUNT(*) FROM audit_log_detail WHERE tenant_id = #{tenantId}")
    Long countByTenantId(@Param("tenantId") String tenantId);

    // ==================== 复合条件查询 ====================

    /**
     * 根据多个条件查询详情
     *
     * @param auditLogId 审计日志ID（可选）
     * @param detailType 详情类型（可选）
     * @param detailKey  详情键（可选）
     * @param isSensitive 是否敏感（可选）
     * @return 详情列表
     */
    default List<AuditLogDetail> selectByConditions(Long auditLogId, String detailType, String detailKey, 
                                                    Boolean isSensitive) {
        LambdaQueryWrapper<AuditLogDetail> wrapper = new LambdaQueryWrapper<>();
        
        if (auditLogId != null) {
            wrapper.eq(AuditLogDetail::getAuditLogId, auditLogId);
        }
        if (detailType != null && !detailType.trim().isEmpty()) {
            wrapper.eq(AuditLogDetail::getDetailType, detailType);
        }
        if (detailKey != null && !detailKey.trim().isEmpty()) {
            wrapper.eq(AuditLogDetail::getDetailKey, detailKey);
        }
        if (isSensitive != null) {
            wrapper.eq(AuditLogDetail::getIsSensitive, isSensitive);
        }

        wrapper.orderByDesc(AuditLogDetail::getCreatedAt);
        return selectList(wrapper);
    }
}