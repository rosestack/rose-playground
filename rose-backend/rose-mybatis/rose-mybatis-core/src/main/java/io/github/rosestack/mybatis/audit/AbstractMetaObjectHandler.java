package io.github.rosestack.mybatis.audit;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;

/** 元数据处理器（核心抽象，不依赖 Spring 配置），由 Starter 注入属性与上下文实现类。 */
public abstract class AbstractMetaObjectHandler implements MetaObjectHandler {

    protected abstract String getCreateTimeColumn();

    protected abstract String getUpdateTimeColumn();

    protected abstract String getCreatedByColumn();

    protected abstract String getTenantColumn();

    protected abstract String getCurrentTenantId();

    protected abstract String getCurrentUsername();

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, getCreateTimeColumn(), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, getCreatedByColumn(), String.class, getCurrentUsername());
        this.strictInsertFill(metaObject, getUpdateTimeColumn(), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, getTenantColumn(), String.class, getCurrentTenantId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictUpdateFill(metaObject, getUpdateTimeColumn(), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, getCreatedByColumn(), String.class, getCurrentUsername());
    }
}
