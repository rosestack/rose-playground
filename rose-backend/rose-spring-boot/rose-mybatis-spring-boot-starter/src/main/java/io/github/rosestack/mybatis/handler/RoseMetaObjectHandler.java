package io.github.rosestack.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * Rose 元数据处理器
 * <p>
 * 实现 MyBatis Plus 的元数据处理接口，自动填充实体的审计字段。
 * 支持创建时间、更新时间、租户ID等字段的自动填充。
 * 用户相关字段（创建人、更新人）应该由权限模块负责填充。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RoseMetaObjectHandler implements MetaObjectHandler {

    private final RoseMybatisProperties properties;

    /**
     * 插入时的字段填充
     * <p>
     * 在插入数据时自动填充以下字段：
     * - 创建时间 (created_time)
     * - 更新时间 (updated_time)
     * - 租户ID (tenant_id)
     * </p>
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentTenantId = ContextUtils.getCurrentTenantId();

        this.strictInsertFill(metaObject, properties.getFieldFill().getCreateTimeColumn(),
                LocalDateTime.class, now);
        log.debug("填充创建时间: {}", now);

        this.strictInsertFill(metaObject, properties.getFieldFill().getUpdateTimeColumn(),
                LocalDateTime.class, now);
        log.debug("填充更新时间: {}", now);

        this.strictInsertFill(metaObject, properties.getTenant().getColumn(),
                String.class, currentTenantId);
        log.debug("填充租户ID: {}", currentTenantId);
    }

    /**
     * 更新时的字段填充
     * <p>
     * 在更新数据时自动填充以下字段：
     * - 更新时间 (updated_time)
     * </p>
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        // 填充更新时间
        this.strictUpdateFill(metaObject, properties.getFieldFill().getUpdateTimeColumn(), LocalDateTime.class, now);
        log.debug("填充更新时间: {}", now);
    }

}
