package io.github.rosestack.spring.boot.mybatis.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import io.github.rosestack.mybatis.tenant.TenantContextHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/** 核心层租户处理器抽象（不直接依赖 Spring 属性），Starter 负责注入列名与忽略表等策略。 */
public abstract class AbstractTenantLineHandler implements TenantLineHandler {

    protected abstract String getTenantIdColumnInternal();

    protected abstract boolean isIgnoreTableInternal(String tableName);

    @Override
    public Expression getTenantId() {
        String tenantId = TenantContextHolder.getCurrentTenantId();
        return new StringValue(tenantId == null ? "" : tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return getTenantIdColumnInternal();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return isIgnoreTableInternal(tableName);
    }
}
