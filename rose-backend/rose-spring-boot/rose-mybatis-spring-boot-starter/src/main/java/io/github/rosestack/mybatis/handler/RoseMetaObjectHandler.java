package io.github.rosestack.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.support.tenant.TenantContextHolder;
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

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentTenantId = TenantContextHolder.getCurrentTenantId();
        String currentUsername = getCurrentUsername();

        this.strictInsertFill(metaObject, properties.getFieldFill().getCreateTimeColumn(), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, properties.getFieldFill().getCreatedByColumn(), String.class, currentUsername);
        log.debug("填充创建人: {}, 创建时间: {}", now);

        this.strictInsertFill(metaObject, properties.getFieldFill().getUpdateTimeColumn(), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, properties.getFieldFill().getCreatedByColumn(), String.class, currentUsername);
        log.debug("填充更新人: {}, 更新时间: {}", now);

        this.strictInsertFill(metaObject, properties.getTenant().getColumn(),
                String.class, currentTenantId);
        log.debug("填充租户ID: {}", currentTenantId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentUsername = getCurrentUsername();

        this.strictUpdateFill(metaObject, properties.getFieldFill().getUpdateTimeColumn(), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, properties.getFieldFill().getCreatedByColumn(), String.class, currentUsername);

        log.debug("填充更新人: {}, 更新时间: {}", now);
    }

    protected String getCurrentUsername() {
        return properties.getFieldFill().getDefaultUser();
    }

}
